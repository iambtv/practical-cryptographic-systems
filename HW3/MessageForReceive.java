package guang.client;

public class MessageForReceive {
	private long sentTime;
	private int messageID;
	private String message;
	private String senderID;
	
	
	public long getSentTime() {
		return sentTime;
	}
	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}
	public int getMessageID() {
		return messageID;
	}
	public void setMessageID(int messageID) {
		this.messageID = messageID;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSenderID() {
		return senderID;
	}
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}
	
	
}
