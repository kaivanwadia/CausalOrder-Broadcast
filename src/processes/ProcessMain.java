package processes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import messages.MessageInstruction;
import messages.BroadcastType;

public class ProcessMain {

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Incorrect number of arguments provided in ProcessMain");
			System.exit(1);
		}
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		int noOfProcess = Integer.parseInt(br.readLine().split(" ")[0]);
		int routerPort = Integer.parseInt(br.readLine().split(" ")[0]);
		if (noOfProcess == 0) {
			System.err.println("Minimum of 1 processes required");
			System.exit(1);
		}
		System.out.println(noOfProcess);
		System.out.println(routerPort);
		System.out.println("--------------");
		// Read the delays into the matrix
		System.out.println("Delays");
		String temp = br.readLine().split(" ")[0];
		if (!temp.equals("Delays")) {
			System.err.println("Wrong format of config file. Was expecting 'Delays'");
			System.exit(1);
		}
		int[][] delayMatrix = new int[noOfProcess][noOfProcess];
		for (int i = 0; i < noOfProcess; i++) {
			String[] delayStrings = br.readLine().split(" ");
			if (delayStrings.length != noOfProcess) {
				System.err.println("Wrong format in delays");
				System.exit(1);
			}
			int[] row = new int[noOfProcess];
			for (int j = 0; j < noOfProcess; j++) {
				row[j] = Integer.parseInt(delayStrings[j]);
				if (j < i) {
					row[j] = delayMatrix[j][i];
				}
			}
			delayMatrix[i] = row;
		}
		System.out.println("Done reading delays:");
		for (int i = 0; i < noOfProcess; i++) {
			for (int j = 0; j < noOfProcess; j++) {
				System.out.print(delayMatrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("----------------");
		// Read the messages to be sent in the system
		HashMap<Integer, ArrayList<MessageInstruction>> messageMap = new HashMap<Integer, ArrayList<MessageInstruction>>();
		for (int i = 0; i < noOfProcess; i++) {
			messageMap.put(i, new ArrayList<MessageInstruction>());
		}
		System.out.println("Messages");
		temp = br.readLine().split(" ")[0];
		if (!temp.equals("Messages")) {
			System.err.println("Wrong format of config file. Was expecting 'Messages'");
			System.exit(1);
		}
		String line = br.readLine();
		while (!line.equals("EndMessages")) {
			System.out.println(line);
			String[] messageValues = line.split(" ");
			if (messageValues.length < 3) {
				System.err.println("Wrong format of message");
				System.exit(1);
			}
			int srcProcess = Integer.parseInt(messageValues[0]);
			String sType = messageValues[1];
			BroadcastType type = sType.equals("bc")?BroadcastType.BROADCAST:BroadcastType.UNICAST;
			String[] sendTimes = messageValues[2].split(",");
			if (type == BroadcastType.BROADCAST) {
				for (int i = 0; i < sendTimes.length; i++) {
					int sendTime = Integer.parseInt(sendTimes[i]);
					MessageInstruction msgInst = new MessageInstruction(srcProcess, -1, sendTime, type);
					messageMap.get(srcProcess).add(msgInst);
				}
			} else if (type == BroadcastType.UNICAST) {
				
			}
			line = br.readLine();
		}
		System.out.println("Done reading messages:");
		for (int i = 0; i < noOfProcess; i++) {
			System.out.println("Process " + i);
			ArrayList<MessageInstruction> msgInfos = messageMap.get(i);
			for (MessageInstruction msgInst : msgInfos) {
				System.out.println(msgInst.toString());
			}
		}
		System.out.println("----------------");
		// TODO : Remove this line
//		noOfProcess = 2;
//		for (int i = 0; i < noOfProcess; i++) {
//			ProcessNode p = new ProcessNode(i, routerPort, delayMatrix[i]);
//			p.start();
//		}
	}

}
