package DistributedStorageSystem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is a wrapper class for file replications.
 * To perform replication, a server connects to another server and calls fileStore().
 * @author Hiroshi Arai
 *
 */
public class ServerFileStore {

	private static final int DATASIZE = 1024 * 1024;
	private Store storeStub;

	public ServerFileStore(Store stub) {
		this.storeStub = stub;
	}

	public void fileStore(String filename, int clientID) throws IOException {
		File storageDir = new File("storage/" + Integer.toString(clientID) + "/");
		File storedFile = new File(storageDir, filename);
		byte[] buffer = new byte[DATASIZE];
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(storedFile));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int read;
		while ((read = reader.read(buffer)) >= 0) {
			storeStub.store(filename, buffer, read, clientID);
		}
		reader.close();
		outputStream.close();
	}
}
