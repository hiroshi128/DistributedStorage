package DistributedStorageSystem;

import java.io.Serializable;

/**
 * Wrapper class for clientID integer.
 * @author Hiroshi Arai
 *
 */
@SuppressWarnings("serial")
public class ClientID implements Serializable{
	private int clientID;

	public ClientID(int clientID) {
		this.clientID = clientID;
	}

	@Override
	public String toString() {
		return Integer.toString(this.clientID);
	}
	
	public int getID(){
		return this.clientID;
	}
}
