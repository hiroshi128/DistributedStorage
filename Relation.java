package DistributedStorageSystem;

import java.io.Serializable;

/**
 * This class represents a connection relation between a server and a client.
 * 
 * @author Hiroshi Arai
 *
 */
public class Relation implements Serializable {
	private static final long serialVersionUID = 1L;
	private String serverName;
	private int clientID;

	public Relation(String serverName, int clientID) {
		this.serverName = serverName;
		this.clientID = clientID;
	}

	public String getServerName() {
		return serverName;
	}

	public int getClientID() {
		return clientID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relation other = (Relation) obj;
		if (this.clientID == other.clientID && this.serverName.equals(other.serverName)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Server: " + this.serverName + ", Client: " + this.clientID;
	}
}
