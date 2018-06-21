package com.example;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;


import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class UTXO {
	
	MongoDatabase db = mongodconfig.DB;
	MongoCollection<Document> blocks_collection = db.getCollection("blocks");
	
	public JSONArray utxo(String client) throws JSONException{
	
		String query = "db.blocks.aggregate(["+
		        "{ $unwind: \"$txs\" },"+
		        "{ $replaceRoot: { newRoot: \"$txs\" }},"+
		        "{ $match: { \"outputs.receiver\":\"" + client + "\" }},"+
		        "{ $lookup: {"+
		            "from: \"blocks\","+
		            "let: { id: \"$id\", sender: \"$sender\" },"+
		            "pipeline: ["+
		                "{ $unwind: \"$txs\" },"+
		                "{ $project: {"+
		                    "_id: 0,"+
		                    "sender: \"$txs.sender\","+
		                    "inputs: \"$txs.inputs\""+
		                "} },"+
		                "{ $unwind: \"$inputs\" },"+
		                "{ $match: { $expr: {"+
		                    "$and: ["+
		                        "{ $eq: [ \"$inputs.id\", \"$$id\" ]},"+
		                        "{ $eq: [ \"$sender\",\"" + client +"\"]}"+
		                    "]"+
		                "}}}"+
		            "],"+
		            "as: \"spent\""+
		        "}},"+
		        "{ $project: {"+
		            "_id: 0,"+
		            "id: 1,"+
		            "sender: 1,"+
		            "inputs: 1,"+
		            "outputs: 1,"+
		            "timestamp: 1,"+
					"spent: { $cond: {"+
		                "if: { $eq: [ \"$spent\", [] ] },"+
		                "then: false,"+
		                "else: true"+
		            "}}"+
		        "}},"+
		        "{ $match: { spent: false }},"+
		        "{ $sort: { timestamp: 1 }}"+
		    "])";
		
		Bson command = new Document("eval",query);
		Document result = db.runCommand(command);
		JSONObject result_json = new JSONObject(result.toJson());
		JSONArray result_batch = result_json.getJSONObject("retval").getJSONArray("_batch");
		return result_batch;
	}
}
