package processes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import messages.CastingType;
import messages.MessageInstruction;

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
		int[][] delayMatrix = getDelayMatrix(br, noOfProcess);
		HashMap<Integer, ArrayList<MessageInstruction>> messageMap = getMessagesForBroadcastMode(br, noOfProcess);
		if (!br.ready()) {
			System.err.println("Maximum running time of system not given for simulation");
			System.exit(1);
		}
		int maxRunningTime = Integer.parseInt(br.readLine().split(" ")[0]);
		br.close();
		for (int i = 0; i < noOfProcess; i++) {
			BroadcastProcess p = new BroadcastProcess(i, noOfProcess, routerPort, delayMatrix[i], messageMap.get(i), maxRunningTime);
			p.start();
		}
	}
	
	/**
	 * Method to read in the delay matrix
	 * @param br - The buffered reader to read from
	 * @param noOfProcess - The number of processes in the system
	 * @return - The delay matrix
	 * @throws IOException
	 */
	private static int[][] getDelayMatrix(BufferedReader br, int noOfProcess) throws IOException {
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
		return delayMatrix;
	}
	
	/**
	 * Method to get the messages sent in the system in broadcast mode
	 * @param br - The buffered reader to read from
	 * @param noOfProcess - The number of processes in the system
	 * @return - An HashMap of ProcessID to ArrayList of MessageInstruction containing all the messages to be sent by that instruction 
	 * @throws IOException
	 */
	private static HashMap<Integer, ArrayList<MessageInstruction>> getMessagesForBroadcastMode(BufferedReader br, int noOfProcess) throws IOException {
		HashMap<Integer, ArrayList<MessageInstruction>> messageMap = new HashMap<Integer, ArrayList<MessageInstruction>>();
		for (int i = 0; i < noOfProcess; i++) {
			messageMap.put(i, new ArrayList<MessageInstruction>());
		}
		String temp = br.readLine().split(" ")[0];
		if (!temp.equals("Messages")) {
			System.err.println("Wrong format of config file. Was expecting 'Messages'");
			System.exit(1);
		}
		String line = br.readLine();
		while (!line.equals("EndMessages")) {
			String[] messageValues = line.split(" ");
			if (messageValues.length < 3) {
				System.err.println("Wrong format of message");
				System.exit(1);
			}
			int srcProcess = Integer.parseInt(messageValues[0]);
			String sType = messageValues[1];
			if (!sType.equals("bc")) {
				System.err.println("Should only be broadcast 'bc' type messages");
				System.exit(1);
			}
			String[] sendTimes = messageValues[2].split(",");
			for (int i = 0; i < sendTimes.length; i++) {
				int sendTime = Integer.parseInt(sendTimes[i]);
				MessageInstruction msgInst = new MessageInstruction(srcProcess, -1, sendTime);
				messageMap.get(srcProcess).add(msgInst);
			}
			line = br.readLine();
		}
		return messageMap;
	}
}
