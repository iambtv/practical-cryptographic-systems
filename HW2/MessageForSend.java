package guang.client;

public class MessageForSend {

	private String recipient;
	private int messageID;
	private String message;
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient=recipient;
	}
	public int getMessageID() {
		return messageID;
	}
	public void setMessageID(int messageID) {
		this.messageID=messageID;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message=message;
	}
		@Override
	public String toString() {
		return "recipient is "+recipient +"  messageID is "+messageID+"  message is "+message;
	}
	

}
