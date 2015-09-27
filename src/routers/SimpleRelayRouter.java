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

import messages.Message;

public class SimpleRelayRouter implements Runnable {
	Socket[] processSockets;
	BufferedReader[] inStreams;
	PrintWriter[] outStreams;
	ServerSocket serverSocket;
	int noOfProcesses;
	int portNo;
	ArrayList<String> routingRequests;
	public SimpleRelayRouter(int noOfProcess, int portNo) {
//		this.noOfProcesses = noOfProcess;
		this.noOfProcesses = 2;
		this.portNo = portNo;
		this.processSockets = new Socket[this.noOfProcesses];
		this.inStreams = new BufferedReader[this.noOfProcesses];
		this.outStreams = new PrintWriter[this.noOfProcesses];
		this.routingRequests = new ArrayList<String>();
	}
	@Override
	public void run() {
		this.SetupRouter();
		while (true) {
			this.ReceiveNewRoutingRequests();
			this.ExecuteRoutingRequests();
		}
	}
		
	public void ReceiveNewRoutingRequests() {
		try {
			for (int i = 0; i < this.noOfProcesses; i++) {
				if (this.inStreams[i].ready()) {
					this.routingRequests.add(this.inStreams[i].readLine());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ExecuteRoutingRequests() {
		
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
