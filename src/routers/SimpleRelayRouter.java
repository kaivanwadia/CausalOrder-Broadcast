package routers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.google.gson.Gson;

import messages.Message;
import messages.RouterRequest;

public class SimpleRelayRouter implements Runnable {
	// TODO : Change this to a HashMap?? Where the key is the processNo/ID 
	Socket[] processSockets;
	BufferedReader[] inStreams;
	PrintWriter[] outStreams;
	ServerSocket serverSocket;
	int noOfProcesses;
	int portNo;
	Gson gson;
	ArrayList<RouterRequest> routingRequests;
	public SimpleRelayRouter(int noOfProcess, int portNo) {
		this.noOfProcesses = noOfProcess;
		this.portNo = portNo;
		this.processSockets = new Socket[this.noOfProcesses];
		this.inStreams = new BufferedReader[this.noOfProcesses];
		this.outStreams = new PrintWriter[this.noOfProcesses];
		this.gson = new Gson();
		this.routingRequests = new ArrayList<RouterRequest>();
	}
	@Override
	public void run() {
		this.SetupRouter();
		System.out.println("Sent ready msgs to all processes");
		while (true) {
			this.ReceiveNewRoutingRequests();
			this.ExecuteRoutingRequests();
		}
	}
		
	public void ReceiveNewRoutingRequests() {
		try {
			for (int i = 0; i < this.noOfProcesses; i++) {
				if (this.inStreams[i].ready()) {
					String incomingJSONString = this.inStreams[i].readLine();
					RouterRequest request = this.gson.fromJson(incomingJSONString, RouterRequest.class);
					request.startTime = System.currentTimeMillis();
					System.out.println("Received Request : \n" + request.toString());
					this.routingRequests.add(request);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ExecuteRoutingRequests() {
		int size = this.routingRequests.size();
		boolean temp = false;
		for (Iterator<RouterRequest> iterator = this.routingRequests.iterator(); iterator.hasNext(); ) {
			RouterRequest request = iterator.next();
			if (System.currentTimeMillis() - request.startTime >= (request.delayTime * 1000)) {
				temp = true;
				System.out.println("Executing Request : \n" + request.toString());
				this.outStreams[request.destProcess].println(this.gson.toJson(request.message));
				this.outStreams[request.destProcess].flush();
				iterator.remove();
			}
		}
		if (temp) {
			System.out.println("Routing Requests Before: " + size);
			System.out.println("Routing Requests After: " + this.routingRequests.size());
		}
	}
	
	public void SetupRouter() {
		System.out.println("Router Started");
		try {
			this.serverSocket = new ServerSocket(this.portNo);
			int noOfConnections = 0;
			while (noOfConnections < this.noOfProcesses) {
				Socket tempSocket = this.serverSocket.accept();
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
				PrintWriter outputStream = new PrintWriter(tempSocket.getOutputStream());
				String pNo = (String) inputStream.readLine();
				int processNo = Integer.parseInt(pNo);
				this.inStreams[processNo] = inputStream;
				this.outStreams[processNo] = outputStream;
				this.processSockets[processNo] = tempSocket;
				noOfConnections++;
			}
		} catch (IOException e) {
			System.out.println("IOException in the SimpleRelayRouter Setup");
			e.printStackTrace();
		}
		for (int i = 0; i < this.noOfProcesses; i++)
		{
			this.outStreams[i].println("RouterReady");
			this.outStreams[i].flush();
		}
	}
}
