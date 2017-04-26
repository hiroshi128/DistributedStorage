package DistributedStorageSystem;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

/**
 * Wrapper class for continuous traffic checking between a client and a server.
 * By implementing Callable interface, instances can be called asynchronously.
 * @author Hiroshi Arai
 *
 */
public class ClientPing implements Callable<Void> {
	private Ping pingStub;
	private DMgrComm reportLatencyStub;
	private boolean stopPing = false;
	private ClientID clientID;
	private HostName serverName;

	public ClientPing(ClientID clientID, HostName serverName, Ping pingStub, DMgrComm reportLatencyStub) {
		this.clientID = clientID;
		this.serverName = serverName;
		this.pingStub = pingStub;
		this.reportLatencyStub = reportLatencyStub;
	}

	private int ping() throws RemoteException {
		long start = System.currentTimeMillis();
		pingStub.ping(null);
		long end = System.currentTimeMillis();
		return (int) (end - start);
	}

	public void stop() {
		this.stopPing = true;
	}

	/**
	 * For reconnection to another server
	 * @param serverName
	 * @param pingStub
	 * @param reportLatencyStub
	 */
	public void update(HostName serverName, Ping pingStub, DMgrComm reportLatencyStub) {
		this.serverName = serverName;
		this.pingStub = pingStub;
		this.reportLatencyStub = reportLatencyStub;
	}

	/**
	 * ping every 500ms asynchronously to check network traffics
	 */
	@Override
	public Void call() throws Exception {
		int latency;
		ClientDMgrComm report = new ClientDMgrComm(clientID, serverName, reportLatencyStub);
		//while (stopPing == false) {
		while(!Thread.interrupted()){
			latency = this.ping();
			report.reportLatency(latency);
			Thread.sleep(500);
		}
		System.out.println("ping finished");
		return null;
	}
}
