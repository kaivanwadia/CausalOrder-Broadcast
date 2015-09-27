package messages;
import java.util.Arrays;


public class Message {
	public int messageNo;
	public int srcProcess;
	public int destProcess;
	public String messageText;
	public int[] timeStamp;
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
	 * @param timeStmp - The vector clock associated with the message
	 */
	public Message(int msgNo, int srcPr, int destPr, int[] timeStmp, String msgTxt) {
		this.messageNo = msgNo;
		this.srcProcess = srcPr;
		this.destProcess = destPr;
		this.messageText = msgTxt;
		this.timeStamp = timeStmp;
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
	/**
	 * Prints the message to standard out
	 */
	public void printMessage() {
		System.out.println("====Message====");
		System.out.println("No: " + this.messageNo + "\t" + "Src: " + this.srcProcess + "\t to Dest: " + this.destProcess);
		System.out.println("Vector Clock: " + Arrays.toString(this.timeStamp) + "\t text: "+this.messageText);
		System.out.println("===============");
	}
	/**
	 * Returns the complete details of the message as a string
	 * @return
	 */
	public String details() {
		String msgDetails = "====Message====\n"+"No: " + this.messageNo + "\t" + "Src: " + this.srcProcess + "\t to Dest: " + this.destProcess+"\n";
		msgDetails = msgDetails+"Vector Clock: " + Arrays.toString(this.timeStamp)+ "\t text: "+this.messageText+"\n===============\n";
		return msgDetails;
	}
	
	/**
	 * Return the string for the log format i.e 
	 * p_{process id}:{message no}
	 */
	@Override
	public String toString() {
		return "p_" + this.srcProcess + ":" + this.messageNo;
	}
}
