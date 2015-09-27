package processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.google.gson.Gson;

import messages.Message;
import messages.MessageInstruction;
import messages.RouterRequest;

public class ProcessNode extends Thread {
	int processID;
	int noOfProcesses;
	int routerPortNo;
	Socket socketToRouter;
	PrintWriter streamToRouter;
	BufferedReader streamFromRouter;
	int noOfMessagesSent;
	long processStartTime;
	Gson gson;
	ArrayList<MessageInstruction> msgsToSend;
	ArrayList<Message> receivedMessages;
	ArrayList<Message> deliveredMsgList;
	int[] delaysToProcess;
	int[] vectorClock;
	int[] deliveredMessages;
	BufferedWriter fileWriter;
	int lastLogTime;
	int maxRunTime;
	/**
	 * Default constructor to return an object of type ProcessNode 
	 * @param pNo - The process number of the process
	 * @param routerPNo - The router port number to connect to
	 * @param delaysToProc - The delays between this process and every other process in the system
	 * @param msgsToSnd - The information about the messages to send from this process to other processes
	 * @param maxRunningTime - The maximum running time of the process. Used for simulation and not in real life
	 */
	public ProcessNode(int pNo, int noOfProcs, int routerPNo, int[] delaysToProc, ArrayList<MessageInstruction> msgsToSnd, int maxRunningTime) {
		this.socketToRouter = null;
		this.processID = pNo;
		this.noOfProcesses = noOfProcs;
		this.routerPortNo = routerPNo;
		this.streamFromRouter = null;
		this.streamToRouter = null;
		this.noOfMessagesSent = 0;
		this.msgsToSend = msgsToSnd;
		this.receivedMessages = new ArrayList<Message>();
		this.deliveredMsgList = new ArrayList<Message>();
		this.gson = new Gson();
		this.delaysToProcess = delaysToProc;
		this.vectorClock = new int[noOfProcs];
		this.deliveredMessages = new int[noOfProcs];
		this.lastLogTime = -1;
		this.maxRunTime = maxRunningTime;
	}
	@Override
	public void run() {
		this.Setup();
		System.out.println("Running process: " + this.toString());
		while (true) {
			this.updateLogTime();
			this.sendMessagesToRouter();
			this.receiveMessagesFromRouter();
			this.deliverMessages();
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
				int timeElapsed = (int) ((System.currentTimeMillis() - this.processStartTime)/1000);
				if (timeElapsed >= msgInst.sendTime) {
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
	 * 
	 */
	private void deliverMessages() {
		for (Iterator<Message> iterator = this.receivedMessages.iterator(); iterator.hasNext(); ) {
			Message recMsg = iterator.next();
			boolean deliverMsg = true;
			if (this.deliveredMessages[recMsg.srcProcess] - recMsg.vectorClock[recMsg.srcProcess] != 1) {
				deliverMsg = false;
			}
			for (int k = 0; k < this.noOfProcesses; k++) {
				if (k==recMsg.srcProcess) {
					continue;
				}
				if (this.deliveredMessages[k] < recMsg.vectorClock[k]) {
					deliverMsg = false;
					break;
				}
			}
			if (deliverMsg) {
				System.out.println("Deliver message from Process "+recMsg.srcProcess+" to Process "+this.processID);
//				this.deliveredMessages
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
				int timeElapsed = (int) ((System.currentTimeMillis() - this.processStartTime)/1000);
				String incomingJSONString = this.streamFromRouter.readLine();
				Message msg = this.gson.fromJson(incomingJSONString, Message.class);
				String log = "p"+this.processID+" REC "+msg.toString()+"\n";
				this.fileWriter.write(log);
				this.fileWriter.write(msg.details());
				this.fileWriter.flush();
//				System.out.println("Message received on p_"+this.processID+":\n"+msg.toString());
				this.receivedMessages.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Setup the ProcessNode for being part of the system.
	 * Connect to the router and wait for the 'RouterReady' message to
	 * start the execution at the node. 
	 */
	public void Setup() {
		System.out.println("Process " + this.processID + " started. Connecting to router");
		try {
			this.socketToRouter = new Socket("localhost", this.routerPortNo);
			this.streamToRouter = new PrintWriter(this.socketToRouter.getOutputStream());
			this.streamFromRouter = new BufferedReader(new InputStreamReader(this.socketToRouter.getInputStream()));
			this.streamToRouter.println(""+this.processID);
			this.streamToRouter.flush();
			// Wait for start message from router
			String str = (String) this.streamFromRouter.readLine();
			if (!str.equals("RouterReady")) {
				throw new Exception("RouterReady message was not sent correctly");
			}
			// Open Log file
			this.fileWriter = new BufferedWriter(new FileWriter(new File("logs/log_P_"+this.processID+".txt")));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		this.processStartTime = System.currentTimeMillis();
	}
	/**
	 * Method to check if the process needs to shut down and take the
	 * necessary actions if it needs to shut down
	 */
	private boolean checkForSystemEnd() {
		try {
			int timeElapsed = (int) ((System.currentTimeMillis() - this.processStartTime)/1000);
			if (timeElapsed >= this.maxRunTime+1) {
				this.streamToRouter.println("terminate");
				this.streamToRouter.flush();
				String str = (String) this.streamFromRouter.readLine();
				if (!str.equals("terminate-ack")) {
					throw new Exception("terminate-ack message was expected");
				}
				this.streamFromRouter.close();
				this.streamToRouter.close();
				this.socketToRouter.close();
				this.fileWriter.write("Simulation Over\n");
				this.fileWriter.flush();
				this.fileWriter.close();
				System.out.println("Process "+this.processID+" terminated.");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Update the log to create a new division if the time has proceeded
	 * by one second
	 */
	private void updateLogTime() {
		try {
			int timeElapsed = (int) ((System.currentTimeMillis() - this.processStartTime)/1000);
			if (timeElapsed-this.lastLogTime == 1) {
				this.fileWriter.write("----------------\n");
				this.fileWriter.write("Time: "+timeElapsed+"\n\n");
				this.fileWriter.flush();
				this.lastLogTime = timeElapsed;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "p_"+this.processID;
	}
}
