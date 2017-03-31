package DistributedStorageSystem;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote object for file replication.
 * @author Hiroshi Arai
 *
 */
public interface Replication extends Remote {
	void replicateFile(File replicationFile, String destServerName, int clientID) throws RemoteException;
}
