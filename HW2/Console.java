package guang.client;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.zip.CRC32;
   
import javax.crypto.KeyGenerator;

import com.google.gson.Gson;

import sun.misc.BASE64Encoder;
public class Console {
	public static void main(String args[])
	{
		
		new Console(args);
		
	}
	private InputStreamReader isr;
	private BufferedReader br;
	private Map<String,String> RSAKeys;
	private Map<String,String> DSAKeys;
	private String keyFingerprints;
	private int messageCount=0;
	public Console(String args[])
	{
		isr =new InputStreamReader(System.in);
		br=new BufferedReader(isr);
		String user="Guang";
		String server="http://jmessage.server.isi.jhu.edu:80";
		try {
			RSAKeys=RsaCrypt.createRsaKeys();
			DSAKeys=DsaCrypt.createDsaKeys();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*
		sendPublicKeys("Guang",server);
		getAllOnlineUser(server);
		ClientNet netForPublic=new ClientNet(ClientNet.GET_PUBLIC_KEY, "Guang", server);
		System.out.println(netForPublic.getPublicKeys());
		*/
		
		if(argsIsValid(args))
		{
			user=args[5];
			server="http://"+args[1]+":"+args[3];
			System.out.println("server="+server);
			//connect to server and send public keys
			
			if(!sendPublicKeys(user,server))
			{
				System.out.println("failed to link to server");
				System.exit(-1);
			}
			//print all commands
			allCommands();
			//begin operation
			while(true)
			{
				System.out.print("command:");
				String commandString=null;
				try {
					commandString=br.readLine();
					switch(commandString)
					{
						case "q":
						case "quit":
							//logout and exit
							System.exit(0);
							break;
						case "l":
						case "list":
							//get all online user
							getAllOnlineUser(server);
							break;
						case "h":
						case "help":
							allCommands();
							break;
						case "get":
						case "":
							getMessage(user, server);
							break;
						case "k":
						case "keyFingerprints":
							System.out.println(keyFingerprints);
							break;
						default:
						{
							if(commandString.startsWith("c") || commandString.startsWith("compose"))
							{
								String[] Conmmands=commandString.split(" ");
								if(Conmmands.length>=2)
								{
									String sendToWho=commandString.substring(commandString.lastIndexOf(" ")+1);
									System.out.println(sendToWho.trim());
								
									sendMessage(user, server, sendToWho.trim(),inputMessageContent());
								}
							}
							else
							{
								allCommands();
							}
						}
							
								 
							
					}
					
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("get input error, please try again!");
					//System.exit(1);
				}
			}
			
		}else
		{
			//use tips 
			System.out.println("usage:\n");
			System.out.println("-s <server name><*>");
			System.out.println("-p <server port><*>");
			System.out.println("-u <user name><*>");
			System.out.println("-w <user password>");
		}
		
	}
	private boolean argsIsValid(String args[])
	{
		System.out.println(args.length);
		for(String s:args)
			System.out.println(s);
		if(!(args[0].equals("-s") && args[2].equals("-p") && args[4].equals("-u")))
			return false;
		else
			return true;
	}
	private void allCommands()
	{
		//tips
		System.out.println("Available commands:");
		System.out.println("   get (or empty line)  - check for new messages");
		System.out.println("   c(ompose) <user>     - compose a message to <user>");
		System.out.println("   l(ist)               - lists all the users in the system");
		System.out.println("   h(elp)               - prints this listing");
		System.out.println("   k(eyFingerprints)    - show key fingerprints");
		System.out.println("   q(uit)               - exits");
		
	}
	private boolean sendPublicKeys(String user,String server)
	{
		boolean b=true;
		StringBuilder sb=new StringBuilder();
		sb.append(RSAKeys.get(RsaCrypt.PUBLIC_RSA_KEY));
		sb.append("%");
		sb.append(DSAKeys.get(DsaCrypt.PUBLIC_KEY_DSA));
		Gson gson=new Gson();
		SendPublicKey sendKey=new SendPublicKey();
		String publicKeys=sb.toString();
		try {
			keyFingerprints=KeyFingerprints.createFingerprints(publicKeys);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendKey.setKeyData(publicKeys);
		
		
		ClientNet net=new ClientNet(ClientNet.POST_PUBLIC_KEY, user, server);
		b=net.sendData(gson.toJson(sendKey));
		return b;
	}
	private void getAllOnlineUser(String server)
	{
		ClientNet net=new ClientNet(ClientNet.GET_ALL_USER, null, server);
		AllUserName users=net.getAllUser();
		System.out.println("all online users");
		for(String user:users.getUsers())
			System.out.println(user);
	}
	
	private void getMessage(String user,String server) throws IOException
	{
		ClientNet net=new ClientNet(ClientNet.GET_MESSAGE, user, server);
		AllReceiveMessage messages= net.getMessage();
		System.out.println("you have got "+messages.getNumMessages() +" messages");
		System.out.println("messageIndex\tsenderID\tmessageID");
		for(int i=0;i<messages.getMessages().size();i++)
		{
			MessageForReceive temp=messages.getMessages().get(i);
			System.out.println(i+"\t\t"+temp.getSenderID()+"\t\t"+temp.getMessageID());
			
		}
		while(messages.getMessages().size()>0)
		{
			//Integerbr.readLine()
			System.out.println("please input index of message,if you'r input is invalid, while exit seeing message mode");
			String index=br.readLine();
			int indexInt=Integer.parseInt(index);
			if(indexInt<0 || indexInt>messages.getMessages().size())
				break;
			MessageForReceive showingMessage=messages.getMessages().get(indexInt);
			//send request to server for sender's DSA public key
			ClientNet netForPublic=new ClientNet(ClientNet.GET_PUBLIC_KEY, showingMessage.getSenderID(), server);
			Map<String,String> keys=netForPublic.getPublicKeys();
			if(keys!=null)
			{
				String DSAPublicKey=keys.get(ClientNet.PUBLIC_KEY_DSA);
				String MessageContent=decryptMessageContent(showingMessage.getMessage(),DSAPublicKey,this.RSAKeys.get(RsaCrypt.PRIVATE_RSA_KEY));				
				String[] messageInfo=MessageContent.split(":");
				if(messageInfo.length>=2)
				{
					if(showingMessage.getSenderID().equals(messageInfo[0]))
					{
						
						//System.out.println(messageInfo[1]);
						//send a read receipt message to the sender
						if(!messageInfo[1].startsWith(">>>READMESSAGE"))
						{
							System.out.println("sending a read receipt message");
							String messagConten=">>>READMESSAGE "+showingMessage.getMessageID();
							sendMessage(user, server, showingMessage.getSenderID(),messagConten);
							System.out.println("the content of selected message:");
							System.out.println(messageInfo[1]);
						}
						else
					
							System.out.println(showingMessage.getSenderID() + " has seen the message!!");
						
					}
					else
						System.out.println("sender changed!!!");
				}else
				{
					System.out.println("parse message faild!!");
				}
			}else
			{
				System.out.println("faild to get public key，please try again");
			}
			//send 
		}

	}
	public static String decryptMessageContent(String encryptString,String senderDsaPublicKey,String reciverRsaPrivateKey)
	{
		String[] allInfos=encryptString.split(" ");
		String sign=allInfos[2];
		String messageCRC=allInfos[1];
		String AESKeyRsaString=allInfos[0];
		String dsaSignData=encryptString.substring(0, encryptString.lastIndexOf(" "));
		String result=null;
		//validate
		try {
			if(DsaCrypt.dsaValidate(dsaSignData, senderDsaPublicKey, sign))
			{
				//sign is validated
				//user RSA private key to decrypt AESKeyRsaString to get aes key Base64
				byte[] aesKey=RsaCrypt.rsaDecryptByPrivateKey(AESKeyRsaString, reciverRsaPrivateKey);
				byte[] decryptMessageCRC=Aes128Crypt.aes128DecryptNew(messageCRC, aesKey);
				java.util.zip.CRC32 crc32=new CRC32();
				crc32.update(decryptMessageCRC, 0, decryptMessageCRC.length-4);
				long crc=crc32.getValue();
				byte[] crcBytes=longToByteArray(crc);
				int plainBytesLength=decryptMessageCRC.length-4;
				boolean crcValid=true;
				
				for(int i=0;i<4;i++)
					if(decryptMessageCRC[plainBytesLength+i]!=crcBytes[i])
					{
						//System.out.printf(x);
						crcValid=false;
						System.out.println("faild to validate crc32");
					}
				/*
				printHexString(decryptMessageCRC);
				System.out.println();
				printHexString(crcBytes);
				System.out.println();
				*/
				if(crcValid)
				{
					byte[] plainBytes=new byte[plainBytesLength];
					for(int i=0;i<plainBytesLength;i++)
						plainBytes[i]=decryptMessageCRC[i];
					result=new String(plainBytes,"UTF-8");
				}
				/*
				byte[] plainBytes=new byte[plainBytesLength];
				for(int i=0;i<plainBytesLength;i++)
					plainBytes[i]=decryptMessageCRC[i];
				result=new String(plainBytes,"UTF-8");
				*/
			}
			
		} catch (Exception e) {
			System.out.println("messageContent decrypt faild, please check!!");
		}
		return result;
	}
	public static String encryptMessageContent(String MessageContent,String receiverRsaPublicKey,String senderDsaPrivatreKey)
	{
		//encrypt
			//String publicKeyRSA=RSAKeys.get(RsaCrypt.PUBLIC_RSA_KEY);
			//String publicKeyDSA=DSAKeys.get(DsaCrypt.PUBLIC_KEY_DSA);
			BASE64Encoder encoder=new BASE64Encoder();
			KeyGenerator keyGenerator=null;
			String willSendMessage=null;
			try {
				
				//auto generate AES key
				keyGenerator = KeyGenerator.getInstance("AES");
				SecureRandom random = new SecureRandom();
				keyGenerator.init(random);
				Key autoGenAesKey = keyGenerator.generateKey();
				byte[] autoGenAesKeyByte=autoGenAesKey.getEncoded();
				//use RSA encrypt AES key and return base64 of c1
				String c1Base64=RsaCrypt.rsaEncryptByPublicKey(autoGenAesKeyByte, receiverRsaPublicKey);
				//String formattedMessage=user+":"+messageContent;
				byte[] formattedMessageBytes=MessageContent.getBytes("UTF-8");
				java.util.zip.CRC32 crc32=new CRC32();
				crc32.update(formattedMessageBytes);
				long crcResult=crc32.getValue();
				byte[] crcByte=longToByteArray(crcResult);
				byte[] messageCRCBytes=new byte[formattedMessageBytes.length+4];
				for(int i=0;i<formattedMessageBytes.length;i++)
					messageCRCBytes[i]=formattedMessageBytes[i];
				for(int i=0;i<4;i++)
					messageCRCBytes[i+formattedMessageBytes.length]=crcByte[i];			
				//use AES128 encrypt MessageCRC to get Base64 String 			
				//String aeskeyString=encoder.encode(autoGenAesKeyByte);
				String c2base64=Aes128Crypt.aes128EncryptNew(messageCRCBytes, autoGenAesKeyByte);
				String willBeSign=c1Base64+" "+c2base64;
				//use DSA sign the info
				String sign=DsaCrypt.dsaSign(willBeSign, senderDsaPrivatreKey);
				willSendMessage=willBeSign+" "+sign;
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return willSendMessage;
	}
	private String inputMessageContent() throws IOException
	{
		System.out.println("please input message:");
		System.out.println("if you complete, please use new line and input string 'end'");
		String line="";
		StringBuilder sb=new StringBuilder();
		
		while(!(line=br.readLine()).equals("end"))
		{
			sb.append(line+"\n");
		}
		return sb.toString();
	}
	private void sendMessage(String user,String server,String sendtoWho,String messageContent) 
	{
		/*
		System.out.println("please input message:");
		System.out.println("if you complete, please use new line and input string 'end'");
		String line="";
		StringBuilder sb=new StringBuilder();
		
		while(!(line=br.readLine()).equals("end"))
		{
			sb.append(line+"\n");
		}
		*/
		//line=br.readLine();
		//sb.append(line);
		
		String messageCongtent=user+":"+messageContent;
		//System.out.println("messageCongtent is "+messageCongtent);
		System.out.println("sending message, please wait!");
		ClientNet netForPublic=new ClientNet(ClientNet.GET_PUBLIC_KEY, sendtoWho, server);
		Map<String,String> keys=netForPublic.getPublicKeys();
		if(keys!=null)
		{
			String RSAPublicKey=keys.get(ClientNet.PUBLIC_KEY_RSA);
			String encryptContent=encryptMessageContent(messageCongtent, RSAPublicKey, this.DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
			//send Message
			MessageForSend m=new MessageForSend();
			m.setRecipient(sendtoWho);
			m.setMessageID(messageCount++);
			m.setMessage(encryptContent.replaceAll("\r|\n", ""));
			ClientNet net=new ClientNet(ClientNet.POST_MESSAGE, user, server);
			net.sendData(m);
			System.out.println("complete!!");
		}else
		{
			System.out.println("faild to get public key，please try again");
		}
		
	}
	public static byte[] longToByteArray(Long i) {   
		byte[] result = new byte[4];   
		result[0] = (byte)((i >> 24) & 0xFF);
		//System.out.println(x);
		result[1] = (byte)((i >> 16) & 0xFF);
		result[2] = (byte)((i >> 8) & 0xFF); 
		result[3] = (byte)(i & 0xFF);
		return result;
	}
	
	public static void printHexString( byte[] b) {  
		for (int i = 0; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			} 
			System.out.print(hex.toUpperCase() ); 
		} 
	
	}
}
