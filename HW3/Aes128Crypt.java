package guang.client;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
    
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class Aes128Crypt {
	
	
	public static String aes128Encrypt(byte[] input,byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		BASE64Decoder decoder=new BASE64Decoder();
		BASE64Encoder encoder = new BASE64Encoder();
		Cipher cipher=Cipher.getInstance("AES/CTR/PKCS5Padding");
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] ivBytes=cipher.getIV();
		//System.out.println("iv length is "+ivBytes.length);s
		byte[] result=cipher.doFinal(input);
		byte[] allResult = new byte[ivBytes.length+result.length];
		System.arraycopy(ivBytes, 0, allResult, 0, ivBytes.length);  
        System.arraycopy(result, 0, allResult, ivBytes.length, result.length);
		String resultString=encoder.encode(allResult);
		//System.out.println(resultString);
		return resultString;
		
	}
	//AES 128 Decrypt
	public static byte[] aes128Decrypt(String input,byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher=Cipher.getInstance("AES/CTR/PKCS5Padding");
		BASE64Decoder decoder=new BASE64Decoder();
		
		//byte[] keyByte=decoder.decodeBuffer(key);
		
		byte[] inputByte=decoder.decodeBuffer(input);
		byte[] ivBytes=new byte[16];
		System.arraycopy(inputByte,0,ivBytes,0,ivBytes.length);
		byte[] inputContent=new byte[inputByte.length-16];
		System.arraycopy(inputByte,16,inputContent,0,inputContent.length);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		//IvParameterSpec iv = new IvParameterSpec("CaoGuangyixixixi".getBytes());
		IvParameterSpec iv = new IvParameterSpec(ivBytes);		
		cipher.init(Cipher.DECRYPT_MODE, skeySpec,iv);
		byte[] resultByte=cipher.doFinal(inputContent);
		//return new String(resultByte,"UTF-8");
		return resultByte;
	}
	
	
	public static String aes128EncryptNew(byte[] input,byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		byte[] inputAfterPadding=null;
		int paddingLength=0;
		if(input.length%16==0)
		{
			paddingLength=16;						
		}else
		{
			paddingLength=16-input.length/16;		
		}
		inputAfterPadding=new byte[input.length+paddingLength];
		System.arraycopy(input, 0, inputAfterPadding, 0, input.length);
		for(int i=0;i<paddingLength;i++)
		{
			inputAfterPadding[input.length+i]=(byte)paddingLength;
		}
		BASE64Decoder decoder=new BASE64Decoder();
		BASE64Encoder encoder = new BASE64Encoder();
		Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] ivBytes=cipher.getIV();
		//System.out.println("iv length is "+ivBytes.length);
		byte[] result=cipher.doFinal(inputAfterPadding);
		byte[] allResult = new byte[ivBytes.length+result.length];
		System.arraycopy(ivBytes, 0, allResult, 0, ivBytes.length);  
        System.arraycopy(result, 0, allResult, ivBytes.length, result.length);
		String resultString=encoder.encode(allResult);
		//System.out.println(resultString);
		return resultString.replaceAll("\r|\n", "");
		
	}
	//AES 128 Decrypt
	public static byte[] aes128DecryptNew(String input,byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
		BASE64Decoder decoder=new BASE64Decoder();
		
		//byte[] keyByte=decoder.decodeBuffer(key);
		
		byte[] inputByte=decoder.decodeBuffer(input);
		byte[] ivBytes=new byte[16];
		System.arraycopy(inputByte,0,ivBytes,0,ivBytes.length);
		byte[] inputContent=new byte[inputByte.length-16];
		System.arraycopy(inputByte,16,inputContent,0,inputContent.length);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		//IvParameterSpec iv = new IvParameterSpec("CaoGuangyixixixi".getBytes());
		IvParameterSpec iv = new IvParameterSpec(ivBytes);		
		cipher.init(Cipher.DECRYPT_MODE, skeySpec,iv);
		byte[] resultByte=cipher.doFinal(inputContent);
		//return new String(resultByte,"UTF-8");
		int resultLength=resultByte.length;
		int paddingLength=resultByte[resultLength-1];
		for(int i=resultLength-paddingLength;i<paddingLength;i++)
		{
			if(resultByte[i]!=paddingLength)
			{
				System.out.println("Padding is invalid");
				return null;
			}
		}
		byte[] resultWithCrc=new byte[resultLength-paddingLength];
		System.arraycopy(resultByte, 0, resultWithCrc, 0, resultWithCrc.length);
		return resultWithCrc;
	}
	public static void main(String args[])
	{
		String s="Guangzhenmei wojuedeguangzhendehenhaofdjaslfjdakslfjkdsla";
		BASE64Encoder encoder=new BASE64Encoder();
		KeyGenerator keygen=null;
		try {
			keygen = KeyGenerator.getInstance("AES");
			SecureRandom random = new SecureRandom();
			keygen.init(random);
			Key key = keygen.generateKey();
			String inputkey=encoder.encode(key.getEncoded());
			System.out.println(inputkey);
			Map<String,String> keys=RsaCrypt.createRsaKeys();
			
			String encryptKey=RsaCrypt.rsaEncryptByPublicKey(key.getEncoded(), keys.get(RsaCrypt.PUBLIC_RSA_KEY));			
			byte[] inputkey2=RsaCrypt.rsaDecryptByPrivateKey(encryptKey, keys.get(RsaCrypt.PRIVATE_RSA_KEY));
			//System.out.println(inputkey2);
			//String key3=encoder.encode(inputkey2);
			System.out.println(encoder.encode(inputkey2));
			
			String encrypt=null;
			encrypt=Aes128Crypt.aes128EncryptNew(s.getBytes("UTF-8"), inputkey2);
			System.out.println(encrypt);
			
			
			System.out.println(new String(Aes128Crypt.aes128DecryptNew(encrypt, inputkey2),"UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		
		
	}
}
