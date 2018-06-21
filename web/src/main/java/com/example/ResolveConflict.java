package com.example;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class ResolveConflict {
	
	Chain chain = new Chain();
	IsChainValid isChainValid = new IsChainValid();
	HttpHeaders headers = new HttpHeaders();
	MongoDatabase db = mongodconfig.DB;
	MongoCollection<Document> blockchain = db.getCollection("blocks");
	MongoCollection<Document> nodes_collection = db.getCollection("nodes");
	MongoCollection<Document> tx_collection = db.getCollection("transactions");
	
	
	FindIterable<Document> nodes =  nodes_collection.find();
	
	public void resolveConflict() throws JSONException{
		JSONArray newChain, myChain;
		boolean flag = false;
		newChain = myChain = chain.chain();
		
		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_JSON);
		//HttpEntity<String> params = new HttpEntity<>(null,headers);
		
		for(Document node:nodes){
			String URL = String.format(
					"http://%s:%s/chain",
					node.get("host"),node.get("port")
			);
			JSONObject response = new JSONObject(restTemplate.getForObject(URL,String.class));
			JSONArray chains
			  = response.getJSONArray("chain");
			System.out.println(chains);
			System.out.println("enter: " + URL);
			if(response.getBoolean("success")&&isChainValid.isValidChain(chains)){
				System.out.println("first condition : " + URL);
				if((chains.length()>newChain.length())
						|| (chains.length()==newChain.length() && chains.length()>0 
						&& chains.getJSONObject(chains.length()-1).getLong("timestamp")<newChain.getJSONObject(newChain.length()-1).getLong("timestamp"))){
					newChain = chains;
					flag = true;
					System.out.println("pass: " + URL);
				}
			}
		}
		if(flag){
			Bson filter = new Document();
			blockchain.deleteMany(filter);
			List<Document> list = new ArrayList<Document>();
			for(int i=0;i<newChain.length();i++){
				list.add(Document.parse(newChain.getJSONObject(i).toString()));
			}
			blockchain.insertMany(list);
			tx_collection.deleteMany(filter);
		}
	}
}
