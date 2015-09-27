package routers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RouterMain {
	public static void main(String[] args) throws IOException {
		Thread routerThread;
		if (args.length < 1) {
			System.err.println("Incorrect number of arguments provided in ProcessMain");
			System.exit(1);
		}
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		int noOfProcesses = Integer.parseInt(br.readLine());
		int portNo = Integer.parseInt(br.readLine());
		SimpleRelayRouter relayRouter = new SimpleRelayRouter(noOfProcesses, portNo);
		routerThread = new Thread(relayRouter);
		routerThread.start();
	}
}
