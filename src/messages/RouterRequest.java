package messages;
import java.util.Date;


public class RouterRequest {
	public Message message;
	public float delayTime;
	public int destProcess;
	public long startTime;
	/**
	 * Constructor to return an object of type RouterRequest.
	 * @param msg - the message to route
	 * @param delay - the delay before sending the message to the destination process
	 * @param destPr - the destination process of the message
	 */
	public RouterRequest(Message msg, float delay, int destPr) {
		this.message = msg;
		this.delayTime = delay;
		this.destProcess = destPr;
		this.startTime = 0;
	}
	@Override
	public String toString() {
		String request = "Message : " + this.message.toString();
		request = request + "\t Delay: " + this.delayTime + "\t Dest: " + this.destProcess + "\t StartTime: " + this.startTime;
		return request;
	}
}
