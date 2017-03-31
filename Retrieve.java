package DistributedStorageSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.*;

/**
 * Remote object for file retrieval.
 * 
 * @author Hiroshi Arai
 *
 */
public interface Retrieve extends Remote {
	File[] findAllFilesStored(int clientID) throws RemoteException;

	byte[] readFile(String dir, String fileName, int client, int start)
			throws RemoteException, FileNotFoundException, IOException;
}
