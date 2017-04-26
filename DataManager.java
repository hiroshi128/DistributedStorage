package DistributedStorageSystem;

import java.util.List;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DataManager class. This class monitors network traffics between servers and
 * clients, and returns uncongested server. Also, this class is responsible to
 * keep file location information in order to retrieve files later.
 * 
 * @author Hiroshi Arai
 *
 */
public class DataManager implements DMgrComm {
	private static String host;
	private static List<String> serverNameList = new ArrayList<String>();
	private static List<Integer> clientIDList = new ArrayList<Integer>();
	private static HashMap<String, HashMap<Integer, ArrayList<Float>>> latencyMap = new HashMap<String, HashMap<Integer, ArrayList<Float>>>();
	private static HashMap<String, Float> averageLatencies = new HashMap<String, Float>();
	private static HashMap<Integer, String> connectionMap = new HashMap<Integer, String>();
	private static List<String> avoidServers = new ArrayList<String>();
	private static HashMap<String, Relation> storedFileMap = new HashMap<>();
	private static HashMap<String, Relation> replicatedFileMap = new HashMap<>();
	public static final boolean DEV_MODE = false;

	public DataManager() {
	}

	/**
	 * check each servers network traffics.
	 */
	public static void checkCongestion() {
		calculateAvgLatency();
		for (String serverName : serverNameList) {
			for (Integer clientID : clientIDList) {
				if (latencyMap.containsKey(serverName) && latencyMap.get(serverName).containsKey(clientID)) {
					ArrayList<Float> latencyHistory = latencyMap.get(serverName).get(clientID);
					if (DEV_MODE) {
						System.out.println(latencyHistory.get(0));
					}
				}
			}
		}
		return;
	}

	/**
	 * Put flag on a crowded server. After flag is set, other servers will be
	 * selected to store.
	 * 
	 * @param serverName
	 */
	private static void putAvoidFlag(String serverName) {
		if (!avoidServers.contains(serverName)) {
			avoidServers.add(serverName);
		}
	}

	/**
	 * Remove flag on a crowded server when traffic reduced.
	 * 
	 * @param serverName
	 */
	private static void removeAvoidFlag(String serverName) {
		if (avoidServers.contains(serverName)) {
			avoidServers.remove(serverName);
		}
	}

	/**
	 * calculate average latency by 100 times of ping. If traffic becomes heavy,
	 * the server will be set avoid flag.
	 */
	public static void calculateAvgLatency() {
		for (String serverName : serverNameList) {
			for (Integer clientID : clientIDList) {
				float avgLatency = 0.0f;
				if (DEV_MODE) {
					System.out.println(serverName + " " + clientID);
				}
				if (latencyMap.containsKey(serverName)) {
					if (latencyMap.get(serverName).containsKey(clientID)) {
						ArrayList<Float> latencyHistory = latencyMap.get(serverName).get(clientID);
						for (int i = 0; i < Math.min(100, latencyHistory.size()); i++) {
							avgLatency += latencyHistory.get(i);
							if (i == Math.min(100, latencyHistory.size()) - 1 && i != 0) {
								avgLatency /= i;
							}
						}
						averageLatencies.put(serverName, avgLatency);

						// empirically decided the threshold to load-balancing
						// noncrowdedLatency x 1.5 is the threshold
						float noncrowdedLatency = calculateNonCongestedLatency(latencyHistory);
						if (avgLatency > noncrowdedLatency * 1.2) {
							putAvoidFlag(serverName);
						} else {
							removeAvoidFlag(serverName);
						}
						System.out.println("avg:" + avgLatency +" noncrowded: "+noncrowdedLatency);
						if (DEV_MODE) {
							System.out.println("avg:" + avgLatency);
						}
					}
				}
			}
		}
	}

	/**
	 * calculate average non-crowded server latency. Assuming the beginning of
	 * the connection is not crowded.
	 * 
	 * @param latencyHistory
	 * @return
	 */
	private static float calculateNonCongestedLatency(ArrayList<Float> latencyHistory) {
		float nonCongestedLatency = 0.0f;

		int i;
		for (i = 1; i < Math.min(10, latencyHistory.size()); i++) {
			nonCongestedLatency += latencyHistory.get(latencyHistory.size() - i);
		}
		nonCongestedLatency /= i;
		System.out.println("non-crowded latency is " + nonCongestedLatency);

		return nonCongestedLatency;

	}

	/**
	 * main method. Check reported network traffics every 500ms.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		host = (args.length < 1) ? null : args[0];
		if (host == null) {
			System.out.println("usage: java DataManager [hostname]");
			return;
		}

		DataManager dmgr = new DataManager();
		try {
			DMgrComm dMgrCommStub = (DMgrComm) UnicastRemoteObject.exportObject(dmgr, SystemConstant.RMI_DMGRSTUB_PORT);

			Registry registry = LocateRegistry.createRegistry(SystemConstant.RMI_DATAMANAGER_PORT);
			registry.rebind(SystemConstant.DMGR_BIND_NAME, dMgrCommStub);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		while (true) {
			checkCongestion();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When DataManager receives latency information, it stores to the
	 * latencyMap LatencyMap contains latency history and relationship between
	 * client and server.
	 */
	@Override
	public void notify(float latency, String serverName, int clientID) throws RemoteException {
		try {
			System.out.println("server:" + serverName + " client: " + clientID);
			if (!serverNameList.contains(serverName)) {
				serverNameList.add(serverName);
			}
			if (!clientIDList.contains(clientID)) {
				clientIDList.add(clientID);
			}
			if (!latencyMap.containsKey(serverName)) {
				latencyMap.put(serverName, new HashMap<Integer, ArrayList<Float>>());
			}
			if (!latencyMap.get(serverName).containsKey(clientID)) {
				latencyMap.get(serverName).put(clientID, new ArrayList<Float>());
			}
			// add the most recent latency to the head
			System.out.println("latency:" + serverName + " " + clientID + " " + latency);
			latencyMap.get(serverName).get(clientID).add(0, latency);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	/**
	 * This method returns uncrowded server by searching avoid flags.
	 * 
	 * @return uncrowded server name.
	 */
	private String findUncrowdedServer() {
		String uncrowdedServerName = host;
		for (String srv : serverNameList) {
			if (avoidServers.contains(srv)) {
				continue;
			}
			uncrowdedServerName = srv;
		}
		if (DEV_MODE)
			System.out.println(uncrowdedServerName);
		return uncrowdedServerName;
	}

	/**
	 * client requests server to store a file.
	 */
	@Override
	public String requestServer(int clientID) throws RemoteException {
		String serverName = findUncrowdedServer();
		if (DEV_MODE)
			System.out.println(serverName);
		connectionMap.put(clientID, serverName);

		for (String filename : storedFileMap.keySet()) {
			System.out.println("File: " + filename + ", place: " + storedFileMap.get(filename));
		}
		return serverName;
	}

	/**
	 * Every server register themselves when the server starts.
	 */
	@Override
	public void addServer(String serverName) throws RemoteException {
		if (!serverNameList.contains(serverName)) {
			serverNameList.add(serverName);
		}
	}

	/**
	 * Every server unregister themselves when the server finishes.
	 */
	@Override
	public void removeServer(String serverName) throws RemoteException {
		if (serverNameList.contains(serverName)) {
			serverNameList.remove(serverName);
		}
	}

	/**
	 * Store file locations to retrieve files later. After storing the file, if
	 * there is a file replicated on another server, the replicated file will be
	 * invalidated.
	 */
	@Override
	public void notifyStoreInfo(String fileName, String serverName, int clientID) throws RemoteException {
		Relation rel = new Relation(serverName, clientID);
		if (storedFileMap.containsKey(fileName)) {
			Relation currentSotredRelation = storedFileMap.get(fileName);
			if (currentSotredRelation.equals(rel)) {
				// do nothing
			} else {
				storedFileMap.put(fileName, rel); // update

				if (DEV_MODE)
					System.out.println(rel);
			}
		} else {
			storedFileMap.put(fileName, rel);
		}

		// invalidate already replicated file
		if (replicatedFileMap.containsKey(fileName)) {
			replicatedFileMap.remove(fileName);
		}
	}

	/**
	 * store replicated file locations to retrieve files later.
	 */
	@Override
	public void notifyReplicationInfo(String fileName, String destServerName, int clientID) throws RemoteException {
		Relation replicatedRelation = new Relation(destServerName, clientID);
		if (replicatedFileMap.containsKey(fileName)) {
			Relation currentRplicatedRelateion = replicatedFileMap.get(fileName);

			if (currentRplicatedRelateion.equals(replicatedRelation)) {
				// do nothing
			} else {
				replicatedFileMap.put(fileName, replicatedRelation);

				if (DEV_MODE)
					System.out.println("Replication: " + replicatedRelation);
			}
		} else {
			replicatedFileMap.put(fileName, replicatedRelation);
		}
	}

	/**
	 * find a server to replicate a file.
	 */
	@Override
	public String findReplicationServer(String fileName, String srcServerName, int clientID)
			throws RemoteException, IllegalArgumentException {
		String destServerName;

		for (String serverName : serverNameList) {
			destServerName = serverName;
			if (!srcServerName.equals(destServerName)) {
				return destServerName;
			}
		}
		throw new IllegalArgumentException("Could not find replication server.");
	}

	/**
	 * retrive the file from both the originally stored file and the replicated file.
	 */
	@Override
	public Relation retrieveStoreInfo(String fileName) throws RemoteException {
		if (storedFileMap.containsKey(fileName)) {
			if (replicatedFileMap.containsKey(fileName)) {
				Relation replicatedFilseServerInfo = replicatedFileMap.get(fileName);
				return replicatedFilseServerInfo;
			}
			Relation fileServerInfo = storedFileMap.get(fileName);
			return fileServerInfo;
		}
		// can not find the file
		return null;
	}

	/**
	 * Clients can check current operating servers by this method.
	 */
	@Override
	public List<String> getServers() throws RemoteException {
		return serverNameList;
	}

	/**
	 * Clients can check uncrowded servers by this method.
	 */
	@Override
	public List<String> getCrowdedServers() throws RemoteException {
		return avoidServers;
	}

	/**
	 * Clients can get information about the location where files are stored.
	 */
	@Override
	public HashMap<String, Relation> getStoredFileMap() throws RemoteException {
		return storedFileMap;
	}

	/**
	 * remove file info for file deletion.
	 */
	@Override
	public void deleteFileInfo(String fileName) throws RemoteException {
		if (storedFileMap.containsKey(fileName)) {
			if (replicatedFileMap.containsKey(fileName)) {
				replicatedFileMap.remove(fileName);
			}
			storedFileMap.remove(fileName);
		}
	}

}
