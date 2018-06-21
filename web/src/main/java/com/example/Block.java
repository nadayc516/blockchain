package com.example;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.math.BigInteger;

import com.mongodb.client.model.Projections;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;


public class Block {
	
	MongoDatabase db = mongodconfig.DB;
	MongoCollection<Document> blockchain = db.getCollection("blocks");
	MongoCollection<Document> tx_collection = db.getCollection("transactions");
	ResolveConflict resolveConflict = new ResolveConflict();
	MerkleRootHash merkleroothash = new MerkleRootHash();
	sha256 SHA256 = new sha256();
	private int difficulty = 4;
	private int resolving_cycle = 100;

	public Document Lastblock(){
		Document lastblock = blockchain.find().sort(Sorts.descending("timestamp")).first();
		return lastblock;
	}
	
	public Document BlockDocument(int nonce) throws JSONException{//후보자 블록
		
		String miner_pubkey = System.getenv().get("MINER_PUBKEY");
		JSONObject block = new JSONObject();
		JSONArray txs = new JSONArray();
		JSONArray outputs = new JSONArray();
		JSONObject o1 = new JSONObject();
		
		o1.put("receiver", miner_pubkey);
		o1.put("amount", 25);
		outputs.put(o1);
		
		JSONObject coin_base = new JSONObject();
		coin_base.put("inputs",new JSONArray());
		coin_base.put("outputs", outputs);
		coin_base.put("timestamp", System.currentTimeMillis());
		coin_base.put("id", SHA256.sha(coin_base.getString("timestamp")));
		txs.put(coin_base);
		
		FindIterable<Document> tx_docs = tx_collection.find().projection(Projections.excludeId());
		for(Document t: tx_docs){
			txs.put(new JSONObject(t.toJson()));
		}

		long time = Instant.now().toEpochMilli();
		block.put("prevBlockHash", calculateHash(Lastblock()));
		block.put("nonce", nonce);
		block.put("timestamp", time);
		block.put("txs", txs);
		block.put("merkleRootHash", MerkleRootHash.getMerkleRoot(block));
		return Document.parse(block.toString());
	}
	
	public void CreateGenesisBlock(){
		sha256 SHA = new sha256();
		Document doc = new Document();
		doc.append("prevBlockHash", SHA.sha("genesis"));
		doc.append("merkleRootHash", SHA.sha("merkleRootHash"));
		doc.append("nonce", 0);
		doc.append("timestamp", System.currentTimeMillis());
	
		blockchain.insertOne(doc);
	}
	
	public String calculateHash(Document d) throws JSONException{
		
		sha256 sha = new sha256();
		System.out.println(d.get("timestamp"));
		String calculatedhash = sha.sha(
				(String)d.get("prevBlockHash") + 
				MerkleRootHash.getMerkleRoot(new JSONObject(d.toJson())) +
				String.valueOf(d.get("timestamp")) +
				String.valueOf(d.get("nonce")));
		return calculatedhash;
	}
	
	public void mineBlock() throws JSONException{
		
		int nonce = 0;
		while(true){
			if (nonce % resolving_cycle == 0) {
				resolveConflict.resolveConflict();
			}
			Document block = BlockDocument(nonce++);
			String hash = calculateHash(block);

			if(IsValidProof(hash)){
				System.out.println("\nIsValid\n");
				blockchain.insertOne(block);
				Bson filter = new Document();
				tx_collection.deleteMany(filter);
				nonce = 0;
			}	
		}
	}
	
	public boolean IsValidProof(String hash){
		String target = new String(new char[difficulty]).replace('\0','0');
		if(hash.substring(0,difficulty).equals(target)){
			return true;
		}
		return false;
	}
}
