package DistributedStorageSystem;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

/**
 * AsynchronousReplication class. This class perform replication to another
 * server asynchronously.
 * 
 * @author Hiroshi Arai
 *
 */

public class AsynchronousReplication implements Callable<Void>, Replication {
	private File storedFile;
	private int clientID;
	private ServerFileStore serverFileStoreOperator;
	private String destServerName;
	private DMgrComm dataManagerCommunicator;

	public AsynchronousReplication(DMgrComm dataManagerCommunicator, ServerFileStore serverFileStoreOperator,
			File storedFile, int clientID, String destServerName) {
		this.dataManagerCommunicator = dataManagerCommunicator;
		this.storedFile = storedFile;
		this.clientID = clientID;
		this.serverFileStoreOperator = serverFileStoreOperator;
		this.destServerName = destServerName;
	}

	/**
	 * This asynchronous method calls replicateFile()
	 */
	@Override
	public Void call() throws Exception {
		replicateFile(storedFile, destServerName, clientID);
		return null;
	}

	/**
	 * This method replicates file to destServerName.
	 */
	@Override
	public void replicateFile(File replicationFile, String destServerName, int clientID) throws RemoteException {
		try {
			serverFileStoreOperator.fileStore(replicationFile.getName(), clientID);
			System.out.println(
					"File " + replicationFile.getName() + " is replicated to " + destServerName + " asynchronously");
			dataManagerCommunicator.notifyReplicationInfo(replicationFile.getName(), destServerName, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
