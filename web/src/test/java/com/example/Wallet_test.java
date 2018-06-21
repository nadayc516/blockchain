//package K;
//
//import java.security.*;
//import org.bouncycastle.jce.interfaces.*;
//import java.security.spec.ECGenParameterSpec;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.bouncycastle.util.encoders.Hex;
//
//public class Wallet {
//	
//	public PrivateKey privateKey;
//	public PublicKey publicKey;
//	
//	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
//	
//	public Wallet() {
//		generateKeyPair();
//	}
//		
//	public static String toHex(byte[] data) {
//	    StringBuilder sb = new StringBuilder();
//	    for (byte b: data) sb.append(String.format("%02x", b&0xff));
//	    return sb.toString();
//	  }
//	public void generateKeyPair() {
//		try {
//			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
//			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
//			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
//			// Initialize the key generator and generate a KeyPair
//			keyGen.initialize(ecSpec); //256 
//	        KeyPair keyPair = keyGen.generateKeyPair();
//	        // Set the public and private keys from the keyPair
//	        privateKey = keyPair.getPrivate();
//	        publicKey = keyPair.getPublic();
//	        
//	        String publicKeyHexValue = Hex.toHexString(publicKey.getEncoded());
//	        String privateKeyHexValue = Hex.toHexString(privateKey.getEncoded());
//	        
//	        //keyGen.
//
//	        System.out.println("publickey : " + publicKeyHexValue);
//	        System.out.println("privatekey : " + privateKeyHexValue);
//	        
//		}catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public float getBalance() {
//		float total = 0;	
//        for (Map.Entry<String, TransactionOutput> item: BlockChain.UTXOs.entrySet()){
//        	TransactionOutput UTXO = item.getValue();
//            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
//            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
//            	total += UTXO.value ; 
//            }
//        }  
//		return total;
//	}
//	
//	public Transaction sendFunds(PublicKey _recipient,float value ) {
//		if(getBalance() < value) {
//			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
//			return null;
//		}
//		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
//		
//		float total = 0;
//		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
//			TransactionOutput UTXO = item.getValue();
//			total += UTXO.value;
//			inputs.add(new TransactionInput(UTXO.id));
//			if(total > value) break;
//		}
//		
//		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
//		newTransaction.generateSignature(privateKey);
//		
//		for(TransactionInput input: inputs){
//			UTXOs.remove(input.transactionOutputId);
//		}
//		
//		return newTransaction;
//	}
//	
//}