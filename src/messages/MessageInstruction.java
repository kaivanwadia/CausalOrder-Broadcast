package messages;

public class MessageInstruction {
	public int srcProcess;
	public int destProcess;
	public int sendTime;
	public MessageInstruction(int sProc, int dProc, int sTime) {
		this.srcProcess = sProc;
		this.destProcess = dProc;
		this.sendTime = sTime;
	}
	
	@Override
	public String toString() {
		return "From: "+this.srcProcess+" to: "+this.destProcess+" at: "+this.sendTime;
	}
}
