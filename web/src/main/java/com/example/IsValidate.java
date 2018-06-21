package com.example;

import java.math.BigInteger;
import java.security.*;

//import java.security.AlgorithmParameters;
//import java.security.KeyFactory;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.Signature;
import org.bouncycastle.jce.interfaces.*;

import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.json.*;

public class IsValidate {
	String WRONG_AMOUNT = "Amount is Wrong!";
	String WRONG_SIGN = "Sign is Wrong!";
	String WRONG_INPUT = "Input is Wrong!";
	
	public IsValidate(){
		Security.addProvider(new BouncyCastleProvider());
	}
	
	MongoDatabase db = mongodconfig.DB;
	MongoCollection<Document> tx_collection = db.getCollection("transactions");
	
	public boolean IsValidAmount(JSONObject json) throws JSONException{
		JSONObject tx = (JSONObject) json.get("transaction");
		//List<JSONArray> inputs = (List<JSONArray>) tx.get("inputs");
		JSONArray inputs = (JSONArray)tx.getJSONArray("inputs");
		JSONArray outputs = (JSONArray)tx.getJSONArray("outputs");
		int index;
		int in_count = 0;
		int out_count = 0;
		for(index=0;index<inputs.length();index++){
			String amount = inputs.getJSONObject(index).get("amount").toString();
			in_count += Integer.parseInt(amount);
		}
		for(index=0;index<outputs.length();index++){
			String amount = outputs.getJSONObject(index).get("amount").toString();
			out_count += Integer.parseInt(amount);
		}
		
		return (in_count==out_count);
	}
	
	public boolean IsValidSign(String msg, String sign, String PublicKey) throws InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		String cuttedKey = PublicKey.substring(2);
		String X_coordinate = cuttedKey.substring(0,64);
		String Y_coordinate = cuttedKey.substring(64);
		
		BigInteger X = new BigInteger(X_coordinate, 16);
		BigInteger Y = new BigInteger(Y_coordinate, 16);
		ECPoint ecPoint = new ECPoint(X, Y);
		//ECPoint pubPoint = new ECPoint(new BigInteger(1,X.toByteArray()),new BigInteger(1, Y.toByteArray()));
		
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "SunEC");
        parameters.init(new ECGenParameterSpec("secp256k1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(ecPoint, ecParameters);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey pubkey = (PublicKey)kf.generatePublic(pubSpec);
        
        try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(pubkey);
			ecdsaVerify.update(msg.getBytes());
			
			return ecdsaVerify.verify(new BigInteger(sign,16).toByteArray());
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
        
	}
	
	public boolean IsValidInputs(JSONObject json) throws JSONException{
		JSONObject tx = (JSONObject) json.get("transaction");
		JSONArray inputs = tx.getJSONArray("inputs");
		UTXO my_utxo = new UTXO();
		JSONArray utxo = my_utxo.utxo(tx.getString("sender"));
		
		for(int i=0;i<inputs.length();i++){
			boolean flag = false;
			for(int j=0;j<utxo.length();j++){
				if(utxo.getJSONObject(j).getString("id").equals(inputs.getJSONObject(i).getString("id"))){
					JSONArray outputs = utxo.getJSONObject(j).getJSONArray("outputs");
					for(int k=0;k<outputs.length();k++){
						if(outputs.getJSONObject(k).getString("receiver").equals(tx.getString("sender"))&&
								outputs.getJSONObject(k).getInt("amount")==inputs.getJSONObject(i).getInt("amount")){
							flag = true;
							break;
						}
					}
				}
				if(flag){
					break;
				}
			}
			if(!flag){
				return false;
			}
		}
		return true;
	}
	
	public String IsValidTx(JSONObject jsonobj) throws JSONException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		
		JSONObject tx = (JSONObject) jsonobj.get("transaction");
		String sender = tx.get("sender").toString();
		String msg = sender + tx.get("timestamp");
		String sign = jsonobj.get("sign").toString();
		
		if(!IsValidAmount(jsonobj)){
			return WRONG_AMOUNT;
		}
		if(!IsValidSign(msg, sign, sender)){
			return WRONG_SIGN;
		}
		if(!IsValidInputs(jsonobj)){
			return WRONG_INPUT;
		}
		
		return null;
	}
	
}
