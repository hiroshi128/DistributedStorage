package DistributedStorageSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Client class. Clients can communicate with DataManager and DataManager will
 * return server name to store, retrieve files. Client works by Command User
 * Interface.
 * 
 * @author Hiroshi Arai
 *
 */
public class Client {
	enum Mode {
		STORE, RETRIEVE, SHOW_DIR, EXIT, UNKNOWN, SHOW_SERVERS, SHOW_CROWDED_SERVERS
	};

	private static Registry dMgrRegistry;
	private static Registry srvRegistry;
	private static Store storeStub;
	private static Retrieve retrieveStub;
	private static Ping pingStub;
	private static NotifyStoreFinished notifyStoreFinishedStub;
	private static DMgrComm dMgrCommStub;
	private static ClientDMgrComm clientDataManagerOperator;
	private static ClientFileStore clientFileStoreOperator;
	private static ClientFileRetrieve clientFileRetrieveOperator;
	private static ClientPing clientPingOperator;
	private static ClientID clientID;
	private static HostName host;
	private static Future<Void> pingStatus;

	/**
	 * main method for Command based file storage client
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			System.out.println("usage: java Client [ClientID] [hostname]");
			return;
		}
		clientID = new ClientID(Integer.parseInt(args[0]));
		host = new HostName(args[1]);

		try {
			connectServer(host);

			// for the background latency checking
			ExecutorService pingService = Executors.newCachedThreadPool();
			pingStatus = pingService.submit(clientPingOperator);

			//start client loop
			Client.Mode currentMode = modeInput();
			while (currentMode != Client.Mode.UNKNOWN && currentMode != Client.Mode.EXIT) {
				switch (currentMode) {
				case STORE:
					HostName requestedServer = clientDataManagerOperator.requestServer();
					if (!requestedServer.getName().equals(host.getName())) {
						connectAnotherServer(requestedServer);
						host = requestedServer;
					}
					System.out.println("Connected to " + host);

					DirectoryViewer.showDirectory(); // to choose files.
					File selected = chooseStoreFile();
					if (selected != null) {
						System.out.println("selected file: " + selected.getName());
					}

					try {
						clientFileStoreOperator.fileStore(selected, clientID);
					} catch (IOException e) {
						System.out.println("File not found");
					}
					break;
				case RETRIEVE:
					System.out.println("Your files on the server:");
					HashMap<String, Relation> storedFileMap = dMgrCommStub.getStoredFileMap();
					
					for(String filename: storedFileMap.keySet()){
						if(storedFileMap.get(filename).getClientID() == clientID.getID()){
							System.out.println(filename);
						}
					}
					System.out.println("Please input file name to retrieve > ");
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String input = in.readLine();

					File retrievedFile = new File(input);
					try {
						HostName storedServerName = clientDataManagerOperator.findStoredServer(retrievedFile.getName());
						if (storedServerName != null && storedServerName != host) {
							System.out.println("Change connection to " + storedServerName + " from " + host);
							connectAnotherServer(storedServerName);
							host = storedServerName;
						}
						System.out.println("Connected to " + host);
						clientFileRetrieveOperator.fileRetrieve(input, clientID, retrievedFile);
					} catch (RemoteException e) {
						System.out.println("File retrieve error");
					}
					break;
				case SHOW_DIR:
					DirectoryViewer.showDirectory();
					break;
				case SHOW_SERVERS:
					List<String> currentServers = dMgrCommStub.getServers();
					System.out.println(currentServers.toString());
					break;
				case SHOW_CROWDED_SERVERS:
					List<String> crowdedServers = dMgrCommStub.getCrowdedServers();
					System.out.println(crowdedServers.toString());
				default:
					// do nothing
				}
				currentMode = modeInput();
			}

			clientPingOperator.stop(); // stop ping
			pingService.shutdown();
			return;

		} catch (RemoteException e) {
			System.err.println("Client exception:" + e.toString());
			e.printStackTrace();
		}
	}

	//Accept user input.
	private static Mode modeInput() throws IOException {
		Client.Mode mode;
		System.out.println(
				"Please input mode(Store[s], Retrieve[r], ShowDirectory[sd], ShowServers[ss], ShowCrowdedServers[sc], Exit[e]):");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = in.readLine();
		if (input == null) {
			mode = Client.Mode.UNKNOWN;
		} else
			switch (input) {
			case "Store":
			case "S":
			case "s":
			case "store":
				mode = Client.Mode.STORE;
				break;
			case "Retrieve":
			case "R":
			case "r":
			case "retrieve":
				mode = Client.Mode.RETRIEVE;
				break;
			case "ShowDirectory":
			case "SD":
			case "sd":
			case "showdirectory":
				mode = Client.Mode.SHOW_DIR;
				break;
			case "Exit":
			case "E":
			case "e":
			case "exit":
				mode = Client.Mode.EXIT;
				break;
			case "ShowServers":
			case "showservers":
			case "ss":
			case "SS":
				mode = Client.Mode.SHOW_SERVERS;
				break;
			case "ShowCrowdedServers":
			case "sc":
			case "SC":
				mode = Client.Mode.SHOW_CROWDED_SERVERS;
				break;
			default:
				mode = Client.Mode.UNKNOWN;
				break;
			}
		return mode;
	}

	// choose store file by CUI
	static private File chooseStoreFile() throws IOException {
		System.out.println("Please enter the file name to store.");
		System.out.print("> ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String fileName = in.readLine();
		File selectedFile = new File(fileName);
		if (selectedFile.exists()) {
			return selectedFile;
		} else {
			return null;
		}
	}

	// connect to the DataManager and the server on the runtime argument.
	private static boolean connectServer(HostName hostName) {
		try {
			dMgrRegistry = LocateRegistry.getRegistry(hostName.getName(), SystemConstant.RMI_DATAMANAGER_PORT);
			srvRegistry = LocateRegistry.getRegistry(hostName.getName(), SystemConstant.RMI_SERVER_PORT);
			setUpStubs();
		} catch (RemoteException e) {
			System.out.println("connect error");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// for load-balancing
	private static boolean connectAnotherServer(HostName serverHost) {

		try {
			pingStatus.cancel(true);
			srvRegistry = LocateRegistry.getRegistry(serverHost.getName(), SystemConstant.RMI_SERVER_PORT);
			setUpStubs();

			clientPingOperator.update(serverHost, pingStub, dMgrCommStub);
			ExecutorService pingService = Executors.newCachedThreadPool();
			pingStatus = pingService.submit(clientPingOperator);
		} catch (RemoteException e) {
			System.out.println("connect error");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//setup RMI stubs.
	private static void setUpStubs() {
		try {
			notifyStoreFinishedStub = (NotifyStoreFinished) srvRegistry.lookup(SystemConstant.STOREFINISHED_BIND_NAME);
			storeStub = (Store) srvRegistry.lookup(SystemConstant.STORE_BIND_NAME);
			clientFileStoreOperator = new ClientFileStore(storeStub, notifyStoreFinishedStub);
			retrieveStub = (Retrieve) srvRegistry.lookup(SystemConstant.RETRIEVE_BIND_NAME);
			clientFileRetrieveOperator = new ClientFileRetrieve(retrieveStub);
			pingStub = (Ping) srvRegistry.lookup(SystemConstant.PING_BIND_NAME);
			dMgrCommStub = (DMgrComm) dMgrRegistry.lookup(SystemConstant.DMGR_BIND_NAME);
			clientDataManagerOperator = new ClientDMgrComm(clientID, host, dMgrCommStub);
			clientPingOperator = new ClientPing(clientID, host, pingStub, dMgrCommStub);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}