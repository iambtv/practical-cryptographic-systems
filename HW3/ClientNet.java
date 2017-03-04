package guang.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
public class ClientNet {
	public static final int POST_PUBLIC_KEY=1;
	public static final int POST_MESSAGE=2;
	public static final int GET_PUBLIC_KEY=3;
	public static final int GET_MESSAGE=4;
	public static final int GET_ALL_USER=5;
	public static final String PUBLIC_KEY_RSA="RsaPublicKey"; 
	public static final String PUBLIC_KEY_DSA="DsaPublicKey";
	//public static final int GET_
	private URL url;
	/**
	 * 
	 * @param type POST_PUBLIC_KEY, POST_MESSAGE, GET_PUBLIC_KEY, GET_MESSAGE, GET_ALL_USER 
	 * @param user must be you , or who you want to know his message and infos 
	 * @param server must be like http://www.hahah.com:80 or http://192.168.1.1:8080
	 */
	public ClientNet(int type,String user,String server)
	{
		String urlString="";
		switch(type)
		{
			case 	POST_PUBLIC_KEY:
				urlString=server+"/registerKey/"+user;
				break;
			case 	POST_MESSAGE:
				urlString=server+"/sendMessage/"+user;
				break;
			case 	GET_PUBLIC_KEY:
				urlString=server+"/lookupKey/"+user;
				break;
			case 	GET_MESSAGE:
				urlString=server+"/getMessages/"+user;
				break;
			case 	GET_ALL_USER:
				urlString=server+"/lookupUsers";
				break;
		}
		try {
			//System.out.println(urlString);
			url=new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("url is not valid!!");
			e.printStackTrace();
		}
	}
	private boolean doPost(String data) throws ServerInnerErrorException
	{
		boolean sendSucceed=true;
		try {
			HttpURLConnection  connection=null;
			OutputStreamWriter out=null;
			connection=(java.net.HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			out= new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			out.write(data);
			//System.out.println(data);
			out.flush();
			out.close();
			//System.out.println(connection.getResponseCode());
			if(connection.getResponseCode()!=200)
				throw new ServerInnerErrorException();
			BufferedReader in = new BufferedReader(new InputStreamReader(  
					connection.getInputStream(), "UTF-8"));  
            String line;  
            // get the data from the server, the data must be a line which contains a JSON object
            while ((line = in.readLine()) != null) {  
                System.out.println(line);
                //in the i will got a message
            }  
            in.close();
		}
		catch (IOException e)
		{
			sendSucceed=false;
			System.out.println("occur a exception!!!");
			e.printStackTrace();
		}
		return sendSucceed;
	}
	private String doGet() throws ServerInnerErrorException
	{
		String result=null;
		try {
			HttpURLConnection  connection=null;
			connection=(java.net.HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");			
			connection.setDoInput(true);
			//System.out.println(connection.getResponseCode());
			BufferedReader in = new BufferedReader(new InputStreamReader(  
					connection.getInputStream(), "UTF-8"));  
            String line;
            if(connection.getResponseCode()!=200)
				throw new ServerInnerErrorException();
            // get the data from the server, the data must be a line which contains a JSON object
            StringBuilder sb=new StringBuilder();
            while ((line = in.readLine()) != null) {  
                //System.out.println(line);
                sb.append(line);
            }  
            in.close();
            result=sb.toString();
		}
		catch (IOException e)
		{
			System.out.println("occur a exception!!!");
			e.printStackTrace();
		}
		return result;
	}
	
	public URL getUrl() {
		return url;
	}
	
	//POST_MESSAGE
	public boolean sendData(MessageForSend message)
	{
		boolean isSendSuccess=true;
		com.google.gson.Gson gson=new Gson();
		String sendData=gson.toJson(message);
		System.out.println("sendData"+sendData);
		try {
			isSendSuccess=doPost(sendData);
		} catch (ServerInnerErrorException e) {
			// TODO Auto-generated catch block
			isSendSuccess=false;
			System.out.println("Server occur a innerError, failed to send message, please try again!");
			//System.out.println("sendData"+sendData);
			e.printStackTrace();
		}
		return isSendSuccess;
	}
	
	//POST_PUBLIC_KEY
	public boolean sendData(String publicKeys)
	{
		boolean isSendSuccess=true;
		try {
			isSendSuccess=doPost(publicKeys);
		} catch (ServerInnerErrorException e) {
			// TODO Auto-generated catch block
			isSendSuccess=false;
			System.out.println("Server occur a innerError, failed to send public key, please try again!");
			e.printStackTrace();
		}
		return isSendSuccess;
	}
	public AllUserName getAllUser()
	{
		String result=null;
		AllUserName users=null;
		try {
			result=doGet();
			com.google.gson.Gson gson=new Gson();
			users = gson.fromJson(result, AllUserName.class);
		} catch (ServerInnerErrorException e) {
			// TODO Auto-generated catch block
			System.out.println("Server occur a innerError, failed to get online users, please try again!");
			e.printStackTrace();
		}
		return users;
	}
	//GET_PUBLIC_KEY
	public Map<String,String> getPublicKeys()
	{
		String result=null;
		try {
			result=doGet();			
		} catch (ServerInnerErrorException e) {
			// TODO Auto-generated catch block
			System.out.println("Server occur a innerError, failed to get user's public key, please try again!");
			e.printStackTrace();
		}
		Gson gson=new Gson();
		ReceiverPublicKey receiverPublicKey=gson.fromJson(result, ReceiverPublicKey.class);
		String keys=receiverPublicKey.getKeyData();
		if(!keys.equals(""))
		{
			String[] keySet=keys.split("%");
			Map<String,String> map=new HashMap<>();
			map.put(ClientNet.PUBLIC_KEY_RSA, keySet[0]);
			map.put(ClientNet.PUBLIC_KEY_DSA, keySet[1]);
			return map;
		}else
		{
			return null;
		}
		
	}
	
	public AllReceiveMessage getMessage()
	{
		String result=null;
		AllReceiveMessage message=null;
		try {
			result=doGet();
			com.google.gson.Gson gson=new Gson();
			message = gson.fromJson(result, AllReceiveMessage.class);
		} catch (ServerInnerErrorException e) {
			// TODO Auto-generated catch block
			System.out.println("Server occur a innerError, failed to get online users, please try again!");
			e.printStackTrace();
		}
		return message;
	}
}
