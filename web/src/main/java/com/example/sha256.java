package com.example;

import java.security.MessageDigest;

public class sha256 {
	public String sha(String str){
		String base = str;
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			
			for(int i=0;i<hash.length;i++){
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length()==1) hexString.append('0');
				hexString.append(hex);
			}
			String sha256_str = hexString.toString();
			//System.out.println(hexString.toString());
			return sha256_str;
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
}
