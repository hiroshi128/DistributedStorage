package DistributedStorageSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote object to check latency between client and server.
 * @author Hiroshi Arai
 *
 */
public interface Ping extends Remote {
	float ping(Server srv) throws RemoteException;
}
