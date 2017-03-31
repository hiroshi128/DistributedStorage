package DistributedStorageSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

/**
 * Remote object for communicating to DataManager
 * @author Hiroshi Arai
 *
 */
public interface DMgrComm extends Remote {
	void notify(float latency, String serverName, int clientID) throws RemoteException;

	String requestServer(int clientID) throws RemoteException;

	void addServer(String serverName) throws RemoteException;
	
	void removeServer(String serverName) throws RemoteException;

	void notifyStoreInfo(String fileName, String serverName, int clientID) throws RemoteException;

	Relation retrieveStoreInfo(String fileName) throws RemoteException;
	
	public String findReplicationServer(String fileName, String srcServerName, int clientID) throws RemoteException;
	
	void notifyReplicationInfo(String fileName, String destServerName, int clientID) throws RemoteException;
	
	public List<String> getServers() throws RemoteException;

	public List<String> getCrowdedServers() throws RemoteException;
	
	public HashMap<String, Relation> getStoredFileMap() throws RemoteException;
}
