package DistributedStorageSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.*;

/**
 * Remote object for file deletion
 * 
 * @author Hiroshi Arai
 *
 */

public interface Delete extends Remote {
	void deleteFile(String fileName, int client) throws RemoteException;
}
