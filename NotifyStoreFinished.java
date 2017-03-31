package DistributedStorageSystem;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyStoreFinished extends Remote{
	void notifyStoreFinished(File storedFile, String filename, int clientID) throws RemoteException;
}
