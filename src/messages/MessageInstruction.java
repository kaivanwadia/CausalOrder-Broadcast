package messages;

public class MessageInstruction {
	public int srcProcess;
	public int destProcess;
	public int sendTime;
	public BroadcastType type;
	public MessageInstruction(int sProc, int dProc, int sTime, BroadcastType castType) {
		this.srcProcess = sProc;
		this.destProcess = dProc;
		this.sendTime = sTime;
		this.type = castType;
	}
	public MessageInstruction(int sProc, int sTime, BroadcastType castType) {
		this(sProc, -1, sTime, castType);
	}
	
	@Override
	public String toString() {
		return ""+this.srcProcess+" "+this.type+" "+this.sendTime+" "+this.destProcess;
	}
}
