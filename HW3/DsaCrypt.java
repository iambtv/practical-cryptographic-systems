package guang.client;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
   
public class DsaCrypt {
	public static final String PUBLIC_KEY_DSA="publicKey";
	public static final String PRIVATE_KEY_DSA="privateKey";
	/**
	 * 
	 * @param plain
	 * @param inputPrivateKey
	 * @return dsaSign which is created for validation, a base64 encode String
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static String dsaSign(String plain,String inputPrivateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
	{
		BASE64Encoder encoder=new BASE64Encoder();
		BASE64Decoder decoder=new BASE64Decoder();
		byte[] privateKeyByte=decoder.decodeBuffer(inputPrivateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByte);  
        KeyFactory factory = KeyFactory.getInstance("DSA");  
        PrivateKey createdPrivateKey = factory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("DSA");  
        signature.initSign(createdPrivateKey);  
        signature.update(plain.getBytes("UTF-8"));  
        return encoder.encode(signature.sign()).replaceAll("\r|\n", "");
	}
	/**
	 * 
	 * @param plain
	 * @param inputPublicKey
	 * @param dsaSign
	 * @return true: the dasSign is valid; false ....
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean dsaValidate(String plain,String inputPublicKey,String dsaSign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
	{
		//BASE64Encoder encoder=new BASE64Encoder();
		BASE64Decoder decoder=new BASE64Decoder();
		byte[] publicKeyByte=decoder.decodeBuffer(inputPublicKey);
		byte[] dsaSignByte=decoder.decodeBuffer(dsaSign);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);  
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");    
        PublicKey createPublicKey = keyFactory.generatePublic(keySpec);  
          
        Signature signature = Signature.getInstance("DSA");     
        signature.initVerify(createPublicKey);   
        signature.update(plain.getBytes("UTF-8"));  
          
        return signature.verify(dsaSignByte);
	}
	/**
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static Map<String,String> createDsaKeys() throws NoSuchAlgorithmException  
	{
		BASE64Encoder encoder=new BASE64Encoder();
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");  
        keyPairGen.initialize(1024);    
        KeyPair keyPair = keyPairGen.generateKeyPair();    
        DSAPublicKey publicKey = (DSAPublicKey) keyPair.getPublic();  
        DSAPrivateKey privateKey = (DSAPrivateKey) keyPair.getPrivate();  
        Map<String, String> keyMap = new HashMap<String, String>(2); 
        String publicKeyBase64 =encoder.encode(publicKey.getEncoded()).replaceAll("\r|\n", "");
        String privateKeyBase64=encoder.encode(privateKey.getEncoded()).replaceAll("\r|\n", "");
        keyMap.put(PUBLIC_KEY_DSA, publicKeyBase64);  
        keyMap.put(PRIVATE_KEY_DSA, privateKeyBase64);  
        return keyMap;  
	}
	
	public static void main(String args[])
	{
		try {
			Map<String,String> myCreateKey=createDsaKeys();
			String publicKey=myCreateKey.get(PUBLIC_KEY_DSA);
			String privateKey=myCreateKey.get(PRIVATE_KEY_DSA);
			String data="jfdlasjfdasjdfldjaslfkjasdijfioejfija";
			try {
				String sign=dsaSign(data,privateKey);
				System.out.println(sign);
				System.out.println(dsaValidate(data, publicKey, sign));
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
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
