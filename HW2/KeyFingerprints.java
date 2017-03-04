package guang.client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyFingerprints {
	public static String createFingerprints(String publicKeys) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] publickeys=publicKeys.getBytes("UTF-8");
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(publickeys);
		byte[] result=messageDigest.digest();
		return bytesToHexString(result);
	}
	
	public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb=new StringBuilder();
        String tmp = null;
        for (int i = 0; i < bytes.length; i++) {
            tmp = (Integer.toHexString(bytes[i] & 0xFF));
            if (tmp.length() == 1) {
            	sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString();
    }
}
