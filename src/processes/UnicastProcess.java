package processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import messages.Message;
import messages.MessageInstruction;
import messages.RouterRequest;

public class UnicastProcess extends ProcessNode {
	/**
	 * Default constructor to return an object of type ProcessNode 
	 * @param pNo - The process number of the process
	 * @param routerPNo - The router port number to connect to
	 * @param delaysToProc - The delays between this process and every other process in the system
	 * @param msgsToSnd - The information about the messages to send from this process to other processes
	 * @param maxRunningTime - The maximum running time of the process. Used for simulation and not in real life
	 */
	public UnicastProcess(int pNo, int noOfProcs, int routerPNo, int[] delaysToProc, ArrayList<MessageInstruction> msgsToSnd, int maxRunningTime) {
		super(pNo, noOfProcs, routerPNo, delaysToProc, msgsToSnd, maxRunningTime);
	}
	@Override
	public void run() {
		this.Setup();
		System.out.println("Running process: " + this.toString());
		while (true) {
			this.updateTimeElapsed();
			this.sendMessagesToRouter();
			this.receiveMessagesFromRouter();
			this.deliverMessagesToProcess();
			if (this.checkForSystemEnd()) {
				break;
			}
		}
	}
	/**
	 * Check if any messages need to be sent at the current time.
	 * If yes then generate the messages and send them to the Router for 
	 * routing them to the destination.
	 */
	private void sendMessagesToRouter() {
		try {
			for (Iterator<MessageInstruction> iterator = this.msgsToSend.iterator(); iterator.hasNext(); ) {
				MessageInstruction msgInst = iterator.next();
				if (this.timeElapsed >= msgInst.sendTime) {
					Message msgToSend = null;
					RouterRequest routerRequest = null;
					int msgNo = this.noOfMessagesSent+1;
					this.vectorClock[this.processID] += 1;
					for (int i = 0; i < this.noOfProcesses; i++) {
						msgToSend = new Message(msgNo, this.processID, i, this.vectorClock, ""+msgNo+" from "+this.processID+" to "+i);
						routerRequest = new RouterRequest(msgToSend, this.delaysToProcess[i], i);
						this.streamToRouter.println("routerequest "+this.gson.toJson(routerRequest));
						this.streamToRouter.flush();
					}
					String log = "p"+this.processID+" BRC "+msgToSend.toString()+"\n";
					this.fileWriter.write(log);
					this.fileWriter.flush();
					this.noOfMessagesSent++;
					iterator.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Go through every message and check if it needs to be delivered.
	 * If it can be delivered log the delivery and remove it from the 
	 * received message list and add it to the deliveredMsgs List.
	 */
	private void deliverMessagesToProcess() {
		for (Iterator<Message> iterator = this.receivedMessages.iterator(); iterator.hasNext(); ) {
			Message recMsg = iterator.next();
			boolean deliverMsg = true;
			if (recMsg.timeStamp[recMsg.srcProcess] - this.deliveredMessages[recMsg.srcProcess] != 1) {
				deliverMsg = false;
			}
			for (int k = 0; k < this.noOfProcesses; k++) {
				if (k==recMsg.srcProcess) {
					continue;
				}
				if (this.deliveredMessages[k] < recMsg.timeStamp[k]) {
					deliverMsg = false;
					break;
				}
			}
			if (deliverMsg) {
				this.deliveredMessages[recMsg.srcProcess] = recMsg.timeStamp[recMsg.srcProcess];
				for (int k = 0; k < this.noOfProcesses; k++) {
					this.vectorClock[k] = Math.max(this.vectorClock[k], recMsg.timeStamp[k]);
				}
				String log = "p"+this.processID+" DLR "+recMsg.toString()+"\n";
				try {
					this.fileWriter.write(log);
					this.fileWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.deliveredMsgList.add(recMsg);
				iterator.remove();
			}
		}
	}
	/**
	 * Receive messages from the router as and when they come in.
	 * Store the messages in an ArrayList to be processed for delivery to the
	 * node.
	 */
	private void receiveMessagesFromRouter() {
		try {
			if (this.streamFromRouter.ready()) {
				String incomingJSONString = this.streamFromRouter.readLine();
				Message msg = this.gson.fromJson(incomingJSONString, Message.class);
				String log = "p"+this.processID+" REC "+msg.toString()+"\n";
				this.fileWriter.write(log);
				this.fileWriter.flush();
				this.receivedMessages.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
