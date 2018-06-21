package com.example;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

@Controller
public class webcontroller{

	IsValidate isValidate = new IsValidate();
	CreateTx Create_tx = new CreateTx();
	Block block = new Block();
	
	public String success() throws JSONException{
		JSONObject Info = new JSONObject();
		Info.put("success", true);
		return Info.toString();
	}
	
	public String fail(Exception e) throws JSONException{
		JSONObject Info = new JSONObject();
		Info.put("success", false);
		Info.put("message", e.getMessage());
		return Info.toString();
	}
	
	@RequestMapping(path = "/transaction/create",method = RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public String createtransaction(@RequestBody String json) throws JSONException, UnknownHostException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		JSONObject response = new JSONObject();
		try{
			JSONObject jsonObj = new JSONObject(json);
	
			response = Create_tx.createTx(jsonObj);
			response.put("success", true);
			if(response.getBoolean("result")==true){
				response.put("message", "New transaction is created");
			}else{
				response.put("message", "transaction is invalid");
			}
		}catch(MongoException b){
			return fail(b);
		}
			
		return response.toString();
	}
	
	@RequestMapping(path = "/blockgenesis/create",method = RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public String createblock() throws JSONException{
		try{
			block.CreateGenesisBlock();
			
		}catch(MongoException b){
			return fail(b);
		}
		return success();	
	}
	
	@RequestMapping(path = "/transaction/validate",method = RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public String validate(@RequestBody String Json) throws JSONException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		JSONObject jsonObj = new JSONObject(Json);
		JSONObject response = new JSONObject();
		
		String message = isValidate.IsValidTx(jsonObj);
		
		if(message!=null){
			response.put("success", true);
			response.put("validate", false);
			response.put("nodeNum", System.getenv().get("NODENUM"));
			response.put("message",message);
		}else{
			response.put("success", true);
			response.put("validate", true);
			response.put("nodeNum", System.getenv().get("NODENUM"));
		}
		//System.out.println(response);
		return response.toString();
	}
	
	@RequestMapping(path = "/transaction/confirm",method = RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public String confirm(@RequestBody String Json) throws JSONException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		JSONObject jsonObj = new JSONObject(Json);
		JSONObject response = new JSONObject();
		Confirm confirm = new Confirm();
		try{
			confirm.tx_comfirm(jsonObj);
			response.put("success", true);
			response.put("message", "New transaction is created");
		}catch(MongoException b){
			response.put("success", false);
		}
		
		return response.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="/utxo/{client}",method = RequestMethod.GET)
		public String utxo(@PathVariable("client") String client) throws JSONException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		JSONObject response = new JSONObject();
		UTXO my_utxo = new UTXO();
		try{
			JSONArray utxo_array = my_utxo.utxo(client);
			response.put("success", true);
			response.put("utxo", utxo_array);
		}catch(MongoException b){
			response.put("success", false);
		}
		
		return response.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="/chain",method = RequestMethod.GET)
		public String chain() throws JSONException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		JSONObject response = new JSONObject();
		Chain my_chain = new Chain();
		try{
			JSONArray blocks = my_chain.chain();
			response.put("success", true);
			response.put("chain", blocks);
		}catch(MongoException b){
			response.put("success", false);
		}
		return response.toString();
	}
	
}
