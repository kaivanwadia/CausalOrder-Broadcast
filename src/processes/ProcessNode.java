package processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import messages.Message;
import messages.MessageInstruction;

import com.google.gson.Gson;

public class ProcessNode extends Thread {

	protected int processID;
	protected int noOfProcesses;
	protected int routerPortNo;
	protected Socket socketToRouter;
	protected PrintWriter streamToRouter;
	protected BufferedReader streamFromRouter;
	protected int noOfMessagesSent;
	long processStartTime;
	protected Gson gson;
	protected ArrayList<MessageInstruction> msgsToSend;
	protected ArrayList<Message> receivedMessages;
	protected ArrayList<Message> deliveredMsgList;
	protected int[] delaysToProcess;
	protected int[] vectorClock;
	protected int[] deliveredMessages;
	protected BufferedWriter fileWriter;
	protected int lastLogTime;
	protected int maxRunTime;
	protected int timeElapsed;

	public ProcessNode(int pNo, int noOfProcs, int routerPNo, int[] delaysToProc, ArrayList<MessageInstruction> msgsToSnd, int maxRunningTime) {
		this.processID = pNo;
		this.noOfProcesses = noOfProcs;
		this.routerPortNo = routerPNo;
		this.delaysToProcess = delaysToProc;
		this.msgsToSend = msgsToSnd;
		this.maxRunTime = maxRunningTime;
		this.socketToRouter = null;
		this.streamFromRouter = null;
		this.streamToRouter = null;
		this.noOfMessagesSent = 0;
		this.receivedMessages = new ArrayList<Message>();
		this.deliveredMsgList = new ArrayList<Message>();
		this.gson = new Gson();
		this.vectorClock = new int[noOfProcs];
		this.deliveredMessages = new int[noOfProcs];
		this.lastLogTime = -1;
		this.timeElapsed = 0;
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
			// Open the log file
			String path = "logs/log_P_"+this.processID+".txt";
			this.fileWriter = new BufferedWriter(new FileWriter(new File(path)));
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
	protected boolean checkForSystemEnd() {
		try {
			if (this.timeElapsed >= this.maxRunTime+1) {
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
	protected void updateTimeElapsed() {
		try {
			this.timeElapsed = (int) ((System.currentTimeMillis() - this.processStartTime)/1000);
			if (this.timeElapsed-this.lastLogTime == 1) {
				this.fileWriter.write("----------------\n");
				this.fileWriter.write("Time: "+this.timeElapsed+"\n\n");
				this.fileWriter.flush();
				this.lastLogTime = this.timeElapsed;
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