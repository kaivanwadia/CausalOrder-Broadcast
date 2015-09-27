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
import java.util.Date;

import messages.Message;
import messages.RouterRequest;

public class ProcessNode extends Thread {
	Socket socketToRouter;
	int processNo;
	int routerPortNo;
	PrintWriter streamToRouter;
	BufferedReader streamFromRouter;
	int messagesSent;
	long startTime;
	public ProcessNode(int pNo, int routerPNo) {
		this.socketToRouter = null;
		this.processNo = pNo;
		this.routerPortNo = routerPNo;
		this.streamFromRouter = null;
		this.streamToRouter = null;
		this.messagesSent = 0;
	}
	@Override
	public void run() {
		this.Setup();
		while (true) {
			if (System.currentTimeMillis() - this.startTime > 2000 && this.messagesSent == 0) {
				// TODO : Send a JSON encoded string and try to retrieve it there
				int recvProc = this.processNo == 0? 1 : 0;
				int delayTime = 3;
				Message msg = new Message(0, this.processNo, recvProc, new int[]{2, 2}, "dsadsad");
				RouterRequest routeReq = new RouterRequest(msg, delayTime, recvProc);
				this.messagesSent++;
			}
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
			// Wait for start from router
			String str = (String) this.streamFromRouter.readLine();
			System.out.println("Process " + this.processNo + " : " + str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Running process: " + this.toString());
		try {
			Thread.sleep(10000);
			System.out.println("Writing Message object");
			this.streamToRouter.println(new Message().toString());
			this.streamToRouter.flush();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return "p_"+this.processNo;
	}
}
