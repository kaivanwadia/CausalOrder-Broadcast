package processes;

import java.io.BufferedReader;
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

import com.google.gson.Gson;

import messages.Message;
import messages.MessageInstruction;
import messages.RouterRequest;

public class ProcessNode extends Thread {
	Socket socketToRouter;
	int processNo;
	int routerPortNo;
	PrintWriter streamToRouter;
	BufferedReader streamFromRouter;
	int messagesSent;
	long startTime;
	Gson gson;
	ArrayList<MessageInstruction> msgsToSend;
	ArrayList<Message> receivedMessages;
	ArrayList<Message> deliveredMessages;
	int[] delaysToProcess;
	/**
	 * Default constructor to return an object of type ProcessNode 
	 * @param pNo - The process number of the process
	 * @param routerPNo - The router port number to connect to
	 * @param delaysToProc - The delays between this process and every other process in the system
	 * @param msgsToSnd - The information about the messages to send from this process to other processes
	 */
	public ProcessNode(int pNo, int routerPNo, int[] delaysToProc, ArrayList<MessageInstruction> msgsToSnd) {
		this.socketToRouter = null;
		this.processNo = pNo;
		this.routerPortNo = routerPNo;
		this.streamFromRouter = null;
		this.streamToRouter = null;
		this.messagesSent = 0;
		this.msgsToSend = msgsToSnd;
		this.receivedMessages = new ArrayList<Message>();
		this.deliveredMessages = new ArrayList<Message>();
		this.gson = new Gson();
		this.delaysToProcess = delaysToProc;
	}
	@Override
	public void run() {
		this.Setup();
		System.out.println("Running process: " + this.toString());
		while (true) {
//			if (System.currentTimeMillis() - this.startTime > 4000*(this.processNo + 1) && this.messagesSent == 0) {
//				int recvProc = this.processNo == 0? 1 : 0;
//				int delayTime = 5*(this.processNo + 1);
//				Message msg = new Message(0, this.processNo, recvProc, new int[]{2, 2}, "dsadsad");
//				RouterRequest routeReq = new RouterRequest(msg, delayTime, recvProc);
//				this.streamToRouter.println(this.gson.toJson(routeReq));
//				this.streamToRouter.flush();
//				this.messagesSent++;
//			}
			this.sendMessagesToRouter();
			this.receiveMessagesFromRouter();
			this.deliverMessages();
		}
	}
	
	private void sendMessagesToRouter() {
		// TODO Auto-generated method stub
	}
	private void deliverMessages() {
		// TODO Auto-generated method stub
	}
	private void receiveMessagesFromRouter() {
		try {
			if (this.streamFromRouter.ready()) {
				String incomingJSONString = this.streamFromRouter.readLine();
				Message msg = this.gson.fromJson(incomingJSONString, Message.class);
				System.out.println("Message received on p_"+this.processNo+":\n"+msg.toString());
				this.receivedMessages.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void Setup() {
		System.out.println("Process " + this.processNo + " started. Connecting to router");
		try {
			this.socketToRouter = new Socket("localhost", this.routerPortNo);
			this.streamToRouter = new PrintWriter(this.socketToRouter.getOutputStream());
			this.streamFromRouter = new BufferedReader(new InputStreamReader(this.socketToRouter.getInputStream()));
			this.streamToRouter.println(""+this.processNo);
			this.streamToRouter.flush();
			// Wait for start message from router
			String str = (String) this.streamFromRouter.readLine();
			if (!str.equals("RouterReady")) {
				throw new Exception("RouterReady message was not sent correctly");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return "p_"+this.processNo;
	}
}
