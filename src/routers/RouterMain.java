package routers;

public class RouterMain {
	public static void main(String[] args) {
		int routerType = 0;
		Thread routerThread;
		if (args.length == 0) {
			System.err.println("Correct arguments not provided to RouterMain");
			System.exit(1);
		}
		if (args.length > 2) {
			routerType = Integer.parseInt(args[2]);
		}
		switch (routerType) {
		case 0:
			SimpleRelayRouter relayRouter = new SimpleRelayRouter(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			routerThread = new Thread(relayRouter);
			routerThread.start();
			break;
		default:
			System.out.println("Incorrect router type");
			break;
		}
	}
}
