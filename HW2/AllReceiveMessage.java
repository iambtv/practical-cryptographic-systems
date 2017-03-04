package guang.client;

import java.util.List;

public class AllReceiveMessage {
	private int numMessages;
	private List<MessageForReceive> messages;
	
	
	public int getNumMessages() {
		return numMessages;
	}
	public void setNumMessages(int numMessages) {
		this.numMessages = numMessages;
	}
	public List<MessageForReceive> getMessages() {
		return messages;
	}
	public void setMessages(List<MessageForReceive> messages) {
		this.messages = messages;
	}
	
	
}
