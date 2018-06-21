package com.example;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IsChainValid {
	public boolean isValidChain(JSONArray chain) throws JSONException{
		Block block = new Block();
		int index = 1;
		if(chain.length()==0){
			return false;
		}
		Document prevBlock = Document.parse(chain.getJSONObject(0).toString());
		
		while(index<chain.length()){
			if(!(block.calculateHash(prevBlock).equals(chain.getJSONObject(index).getString("prevBlockHash")))
					&&block.IsValidProof(block.calculateHash(Document.parse(chain.getJSONObject(index).toString())))){
				return false;
			}
			prevBlock = Document.parse(chain.getJSONObject(index).toString());
			index++;
		}
		return true;
	}
}
