package processes;

public class ProcessMain {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Incorrect number of arguments provided in ProcessMain");
			System.exit(1);
		}
		int noOfProcess = Integer.parseInt(args[0]);
		int routerPort = Integer.parseInt(args[1]);
		if (noOfProcess == 0) {
			System.err.println("Minimum of 1 processes required");
			System.exit(1);
		}
		// TODO : Remove this line
		noOfProcess = 2;
		for (int i = 0; i < noOfProcess; i++) {
			ProcessNode p = new ProcessNode(i, routerPort);
			p.start();
		}
	}

}
