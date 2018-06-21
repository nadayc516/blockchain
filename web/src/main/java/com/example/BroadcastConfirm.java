package com.example;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class BroadcastConfirm {
	
	HttpHeaders headers = new HttpHeaders();
	
	public void broadcast_confirm(String json){
		MongoDatabase db = mongodconfig.DB;
		MongoCollection<Document> nodes_collection = db.getCollection("nodes");
		
		FindIterable<Document> nodes =  nodes_collection.find();
		
		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> params = new HttpEntity<>(json,headers);

		for(Document node:nodes){
			String URL = String.format(
					"http://%s:%s/transaction/confirm",
					node.get("host"),node.get("port")
			);
			
			String response
			  = restTemplate.postForObject(URL,params,String.class);
		}
	}
}
