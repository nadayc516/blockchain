package com.example;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class Confirm {
	public void tx_comfirm(JSONObject tx) throws JSONException{
		MongoDatabase db = mongodconfig.DB;
		MongoCollection<Document> tx_collection = db.getCollection("transactions");
		Document document = new Document();
		
		document.append("sender", tx.getString("sender"));
		document.append("inputs", JSON.parse(tx.getJSONArray("inputs").toString()));
		document.append("outputs", JSON.parse(tx.getJSONArray("outputs").toString()));
		document.append("timestamp", tx.getLong("timestamp"));
		document.append("id", tx.getString("id"));
		
		tx_collection.insertOne(document);
	}
}
