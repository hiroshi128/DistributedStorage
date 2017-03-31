package DistributedStorageSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote object for file store.
 * 
 * @author Hiroshi Arai
 *
 */
public interface Store extends Remote {
	boolean store(String filename, byte[] data, int length, int clientID) throws RemoteException;
}
