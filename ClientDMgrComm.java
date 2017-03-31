package DistributedStorageSystem;

import java.rmi.RemoteException;

/**
 * Wrapper class to communicate to DataManager from Client.
 * @author Hiroshi Arai
 *
 */
public class ClientDMgrComm {
	private DMgrComm dMgrCommStub;
	private ClientID clientID;
	private HostName serverName;

	public ClientDMgrComm(ClientID clientID, HostName serverName, DMgrComm dMgrCommStub) {
		this.clientID = clientID;
		this.serverName = serverName;
		this.dMgrCommStub = dMgrCommStub;
	}
	
	/**
	 * Report latency between clientID and serverName to the DataManager
	 * @param latency
	 * @throws RemoteException
	 */
	public void reportLatency(float latency) throws RemoteException {
		dMgrCommStub.notify(latency, serverName.getName(), clientID.getID());
	}

	/**
	 * request a server to store a file
	 * @return
	 * @throws RemoteException
	 */
	public HostName requestServer() throws RemoteException {
		String serverName = dMgrCommStub.requestServer(this.clientID.getID());
		return new HostName(serverName);
	}

	/**
	 * find the server which contains fileName File to retrieve the file.
	 * @param fileName
	 * @return
	 * @throws RemoteException
	 */
	public HostName findStoredServer(String fileName) throws RemoteException {
		HostName serverName = null;
		Relation storedInfo = dMgrCommStub.retrieveStoreInfo(fileName);
		if (storedInfo != null) {
			serverName = new HostName(storedInfo.getServerName());
		}
		return serverName;
	}
}
