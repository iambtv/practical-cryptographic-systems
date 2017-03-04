package guang.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
public class RsaCrypt {
	public static final String PUBLIC_RSA_KEY="publicKey";
	public static final String PRIVATE_RSA_KEY="privateKey";
	/**
	 * 
	 * @param plant
	 * @param inputPublicKey base64
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	
	public static String rsaEncryptByPublicKey(byte[] inputData,String inputPublicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		BASE64Encoder encoder=new BASE64Encoder();
		BASE64Decoder decoder=new BASE64Decoder();
		//BASE64Encoder encoder=new BASE64Encoder();
        byte[] keyBytes = decoder.decodeBuffer(inputPublicKey);  
        //byte[] inputDataByte=decoder.decodeBuffer(inputData);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
        //System.out.println(keyFactory.getAlgorithm());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
        //replace
        byte[] result= cipher.doFinal(inputData);
        return encoder.encode(result).replaceAll("\r|\n", "");
	}
	/**
	 * 
	 * @param inputData
	 * @param inputPrivateKey
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] rsaDecryptByPrivateKey(String inputData,String inputPrivateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		
		BASE64Decoder decoder=new BASE64Decoder();
		BASE64Encoder encoder=new BASE64Encoder();
		byte[] inputBytes= decoder.decodeBuffer(inputData);
		byte[] inputPrivateKeyByte = decoder.decodeBuffer(inputPrivateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(inputPrivateKeyByte);  
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
  
        byte[] result=cipher.doFinal(inputBytes);
        
        return result;
        //return encoder.encode(result);
	}
	/**
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static Map<String,String> createRsaKeys() throws NoSuchAlgorithmException  
	{
		BASE64Encoder encoder=new BASE64Encoder();
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");  
        keyPairGen.initialize(1024);    
        KeyPair keyPair = keyPairGen.generateKeyPair();    
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
        Map<String, String> keyMap = new HashMap<String, String>(2); 
        String publicKeyBase64 =encoder.encode(publicKey.getEncoded()).replaceAll("\r|\n", "");
        String privateKeyBase64=encoder.encode(privateKey.getEncoded()).replaceAll("\r|\n", "");
        keyMap.put(PUBLIC_RSA_KEY, publicKeyBase64);  
        keyMap.put(PRIVATE_RSA_KEY, privateKeyBase64);  
        return keyMap;  
	}
	
	public static void main(String args[])
	{
		try {
			Map<String,String> myAllKey=createRsaKeys();
			String myPublicKey=myAllKey.get(PUBLIC_RSA_KEY);
			String myPrivateKey=myAllKey.get(PRIVATE_RSA_KEY);
			System.out.println(myPublicKey);
			System.out.println();
			System.out.println(myPrivateKey);
			String s="guangisbeautifulfdsafdsaf";
			byte[] input=null;
			try {
				input = s.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String result;
			try {
				result = rsaEncryptByPublicKey(input,myPublicKey);
				System.out.println(result);
				String myPlain=new String(rsaDecryptByPrivateKey(result,myPrivateKey),"UTF-8");
				System.out.println(myPlain);
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

