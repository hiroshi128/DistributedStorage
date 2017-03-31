package DistributedStorageSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Wrapper class for file retrieval from Server
 * @author Hiroshi Arai
 *
 */
public class ClientFileRetrieve {
	private Retrieve retrieveStub;

	public ClientFileRetrieve(Retrieve stub) {
		this.retrieveStub = stub;
	}

	/**
	 * Retrieve a file from the connected server
	 * @param fileName
	 * @param clientID
	 * @param dest
	 * @throws IOException
	 */
	public void fileRetrieve(String fileName, ClientID clientID, File dest) throws IOException {
		byte[] buffer = retrieveStub.readFile("storage/" + Integer.toString(clientID.getID()) + "/", fileName,
				clientID.getID(), 0);
		FileOutputStream output = new FileOutputStream(fileName);
		output.write(buffer, 0, buffer.length);
		output.flush();
		output.close();
	}
}
