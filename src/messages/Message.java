package messages;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;


public class Message implements Serializable{
	public int messageNo;
	public int srcProcess;
	public int destProcess;
	public String messageText;
	public int[] vectorClock;
	/**
	 * Default constructor for Message. Sets everything to -1
	 */
	public Message() {
		this(-1, -1, -1, null, "");
	}
	/**
	 * A constructor to return a new object of type message.
	 * @param msgNo - The message number of the message
	 * @param srcPr - The source process number of the message
	 * @param destPr - The destination process number of the message
	 * @param msgTxt - The text of the message
	 * @param vectorClk - The vector clock associated with the message
	 */
	public Message(int msgNo, int srcPr, int destPr, int[] vectorClk, String msgTxt) {
		this.messageNo = msgNo;
		this.srcProcess = srcPr;
		this.destProcess = destPr;
		this.messageText = msgTxt;
		this.vectorClock = vectorClk;
	}
	/**
	 * A constructor to return a new object of type message. The msg text is set to empty.
	 * @param msgNo - The message number of the message
	 * @param srcPr - The source process number of the message
	 * @param destPr - The destination process number of the message
	 * @param vectorClk - The vector clock associated with the message
	 */
	public Message(int msgNo, int srcPr, int destPr, int[] vectorClk) {
		this(msgNo, srcPr, destPr, vectorClk, "");
	}
	
	public void printMessage() {
		System.out.println("====Message====");
		System.out.println("No: " + this.messageNo + "\t" + "Src: " + this.srcProcess + "to Dest: " + this.destProcess);
		System.out.println("Vector Clock: " + Arrays.toString(this.vectorClock));
		System.out.println("===============");
	}
	@Override
	public String toString() {
		return "p_" + this.srcProcess + ":" + this.messageNo;
	}
}
