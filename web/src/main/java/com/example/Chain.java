package com.example;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;

public class Chain {

	public JSONArray chain() throws JSONException{
		MongoDatabase db = mongodconfig.DB;
		MongoCollection<Document> blockchain = db.getCollection("blocks");
		
		String blocks = JSON.serialize(blockchain.find().projection(Projections.excludeId()).sort(Sorts.ascending("timestamp")));
		
		return new JSONArray(blocks);
	}
}
