package DistributedStorageSystem;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HostName implements Serializable{
	private String hostName;
	
	public HostName(String hostName){
		this.hostName = hostName;
	}
	
	public String getName(){
		return this.hostName;
	}
	
	@Override
	public String toString(){
		return this.hostName;
	}
}
