package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MerkleRootHash {
	static sha256 SHA256 = new sha256();
	MongoDatabase db = mongodconfig.DB;
	MongoCollection<Document> blockchain = db.getCollection("blocks");
	
	public static String getMerkleRoot(JSONObject block) throws JSONException {
		int length;
		JSONArray txs = block.getJSONArray("txs");
		if(txs.length()==0){
			return block.getString("merkleRootHash");
		}
		List<String> list = new ArrayList<String>();
		for(int i=0;i<txs.length();i++){
			list.add(txs.getJSONObject(i).getString("id"));
		}
		
		Collections.sort(list, new Comparator<String>(){
			@Override
	        public int compare(String a, String b) {
	            return a.compareTo(b);
			}
		});
		
		while(list.size()!=1){
			if(list.size()%2==1){
				list.add(list.get(list.size()-1));
			}
			length = list.size();
			
			for(int i=0;i<length;i+=2){
				list.add(SHA256.sha(list.get(0)+list.get(1)));
				list.remove(0);
				list.remove(0);
			}
		}
		return list.get(0);
	}
}
