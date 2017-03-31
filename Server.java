package DistributedStorageSystem;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

/**
 * Server class. This server class supports Store, Retrieve, Ping, Replication functions.
 * Each methods can be called by RMI
 * @author Hiroshi Arai
 *
 */
public class Server implements Store, Retrieve, Ping, Replication, NotifyStoreFinished {
	private static Registry dMgrRegistry;
	private static DMgrComm dMgrCommStub;
	private static String serverName;

	// for fault-tolerance
	private static Registry srvRegistry;
	private static Store replicationStoreStub;
	private static ServerFileStore serverFileStoreOperator;

	public Server() {
	}

	public boolean store(String filename, byte[] data, int length, int clientID) {
		try {
			File storageDir = new File("storage/" + Integer.toString(clientID) + "/");
			if (!storageDir.exists()) {
				storageDir.mkdirs();
			}
			File storeFile = new File(storageDir, filename);
			storeFile.createNewFile();
			FileOutputStream out = new FileOutputStream(storeFile); // overwrite
																	// stored
			// file
			out.write(data, 0, length);
			out.flush();
			out.close();

		} catch (Exception e) {
			System.err.println("store error");
			return false;
		}
		return true;
	}

	public static void copyFile(File in, File out) throws IOException {
		FileInputStream fin = new FileInputStream(in);
		FileOutputStream fout = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fin.read(buf)) != -1) {
				fout.write(buf, 0, i);
			}
		} catch (IOException exception) {
			throw exception;
		} finally {
			if (fin != null) {
				fin.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
	}

	public static void main(String args[]) {

		serverName = (args.length < 1) ? null : args[0];
		String dataMangerName = (args.length < 2) ? null : args[1];
		if (serverName == null || dataMangerName == null) {
			System.out.println("usage: java Server [hostname] [dataManagerHostname]");
			return;
		}

		try {
			System.setProperty("java.rmi.server.hostname", serverName);
			Server storeServer = new Server();
			Server retrieveServer = new Server();
			Server checkConnectionSpeedServer = new Server();
			Server replicationServer = new Server();
			Server storeFinishedServer = new Server();
			Store storeStub = (Store) UnicastRemoteObject.exportObject(storeServer, SystemConstant.RMI_STORE_PORT);
			Retrieve retrieveStub = (Retrieve) UnicastRemoteObject.exportObject(retrieveServer,
					SystemConstant.RMI_RETRIEVE_PORT);
			Ping checkConnectionSpeedStub = (Ping) UnicastRemoteObject.exportObject(checkConnectionSpeedServer,
					SystemConstant.RMI_PING_PORT);
			Replication replicationStub = (Replication) UnicastRemoteObject.exportObject(replicationServer,
					SystemConstant.RMI_REPLICATION_PORT);
			NotifyStoreFinished notifyStoreFinishedStub = (NotifyStoreFinished) UnicastRemoteObject
					.exportObject(storeFinishedServer, SystemConstant.RMI_STORE_FINISHED_PORT);

			// Bind the remote object's stub in the registry
			Registry registry;
			registry = LocateRegistry.createRegistry(SystemConstant.RMI_SERVER_PORT);
			registry.rebind(SystemConstant.STORE_BIND_NAME, storeStub);
			registry.rebind(SystemConstant.RETRIEVE_BIND_NAME, retrieveStub);
			registry.rebind(SystemConstant.PING_BIND_NAME, checkConnectionSpeedStub);
			registry.rebind(SystemConstant.REPLICATION_BIND_NAME, replicationStub);
			registry.rebind(SystemConstant.STOREFINISHED_BIND_NAME, notifyStoreFinishedStub);

			// Prepare to connect DataManager
			dMgrRegistry = LocateRegistry.getRegistry(dataMangerName, SystemConstant.RMI_DATAMANAGER_PORT);
			dMgrCommStub = (DMgrComm) dMgrRegistry.lookup(SystemConstant.DMGR_BIND_NAME);
			dMgrCommStub.addServer(serverName);

			// remove this server from DataManager when shutdown
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					dMgrCommStub.removeServer(serverName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}));

			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception:" + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public float ping(Server srv) throws RemoteException {
		try {
			float dummy = 1.0f;
			return dummy;
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public File[] findAllFilesStored(int clientID) throws RemoteException {
		File dir = new File("storage/" + Integer.toString(clientID) + "/");
		File[] files = dir.listFiles();
		return files;
	}

	@Override
	public byte[] readFile(String dir, String fileName, int client, int start) throws IOException, RemoteException {
		File target = new File(dir, fileName);
		if (target.exists() == false) {
			throw new RemoteException("File does not exist");
		}
		if (target.isFile() == false) {
			throw new RemoteException("retrieve supports only files, not directory");
		}

		byte[] b = new byte[(int) target.length()];
		FileInputStream fis = new FileInputStream(target);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while (fis.read(b) > 0) {
				baos.write(b);
			}
			baos.close();
			fis.close();
			b = baos.toByteArray();
		} catch (IOException e) {

		}
		return b;
	}

	@Override
	public void replicateFile(File replicationFile, String destServerName, int clientID) {
		// TODO Auto-generated method stub
		connectAnotherServer(destServerName);
		try {
			serverFileStoreOperator.fileStore(replicationFile.getName(), clientID);
			System.out.println("File " + replicationFile.getName() + " is replicated to " + destServerName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean connectAnotherServer(String destServerName) {
		try {
			srvRegistry = LocateRegistry.getRegistry(destServerName, SystemConstant.RMI_SERVER_PORT);
			replicationStoreStub = (Store) srvRegistry.lookup(SystemConstant.STORE_BIND_NAME);
			serverFileStoreOperator = new ServerFileStore(replicationStoreStub);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void notifyStoreFinished(File storedFile, String filename, int clientID) throws RemoteException {
		// for location transparency
		dMgrCommStub.notifyStoreInfo(filename, serverName, clientID);
		// for replication
		try {
			String replicationServerName = dMgrCommStub.findReplicationServer(filename, serverName, clientID);

			// TODO remember replicatePlaceToRetrieve and next Store
			connectAnotherServer(replicationServerName);

			// replicate asynchronously
			AsynchronousReplication asyncReplication = new AsynchronousReplication(dMgrCommStub,
					serverFileStoreOperator, storedFile, clientID, replicationServerName);

			ExecutorService replicationService = Executors.newCachedThreadPool();
			replicationService.submit(asyncReplication);
		} catch (IllegalArgumentException e) {
			// can not find replication server
			return;
		}

	}

}