package com.example;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;


import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class CreateTx {
	
	HttpHeaders headers = new HttpHeaders();
	IsValidate isValidate = new IsValidate();
	
	public JSONObject createTx(JSONObject json) throws InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, JSONException{
		MongoDatabase db = mongodconfig.DB;
		MongoCollection<Document> nodes_collection = db.getCollection("nodes");
		MongoCollection<Document> tx_collection = db.getCollection("transactions");
		sha256 SHA256 = new sha256();
		
		int consensus = 0;
		boolean result;
		JSONArray jsonArray = new JSONArray();
		JSONObject res_consensus = new JSONObject();
		Document document = new Document();
		BroadcastConfirm confirm = new BroadcastConfirm();
		
		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> params = new HttpEntity<>(json.toString(),headers);
		
		consensus = (isValidate.IsValidTx(json) == null) ? 1 : 0;
		FindIterable<Document> nodes =  nodes_collection.find();
		
		for(Document node:nodes){
			String URL = String.format(
					"http://%s:%s/transaction/validate",
					node.get("host"),node.get("port")
			);
			String response
			  = restTemplate.postForObject(URL,params,String.class);
			
			JSONObject resbody = new JSONObject(response);
			jsonArray.add(resbody);
			
			if(resbody.getBoolean("success")==true&&resbody.getBoolean("validate")==true){
				consensus++;
			}
			
			System.out.println(resbody);
		}
		long size = nodes_collection.count();
		result = ((size +1) /2 < consensus) ? true : false;
		
		if(result==true){
			JSONObject tx = (JSONObject) json.get("transaction");
			document.append("sender", tx.getString("sender"));
			document.append("inputs", JSON.parse(tx.getJSONArray("inputs").toString()));
			document.append("outputs", JSON.parse(tx.getJSONArray("outputs").toString()));
			document.append("timestamp", tx.getLong("timestamp"));
			document.append("id", SHA256.sha(tx.getString("sender")+tx.getLong("timestamp")));
			
			confirm.broadcast_confirm(JSON.serialize(document));
			tx_collection.insertOne(document);
		}
		
		res_consensus.put("result", result);
		res_consensus.put("consensus", consensus);
		res_consensus.put("responses", jsonArray);
		

		return res_consensus;
	}
}

