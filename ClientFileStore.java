package DistributedStorageSystem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Wrapper class for file store from Client.
 * @author Hiroshi Arai
 *
 */
public class ClientFileStore {
	private static final int DATASIZE = 1024 * 1024;
	private Store storeStub;
	private NotifyStoreFinished notifyStoreFinishedStub;

	public ClientFileStore(Store stub, NotifyStoreFinished notifyStoreFinishedStub) {
		this.storeStub = stub;
		this.notifyStoreFinishedStub = notifyStoreFinishedStub;
	}

	/**
	 * Store a file on the coneected server.
	 * @param storeFile
	 * @param clientID
	 * @throws IOException
	 */
	public void fileStore(File storeFile, ClientID clientID) throws IOException {
		byte[] buffer = new byte[DATASIZE];
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(storeFile.getPath()));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int read;
		System.out.println("Uploading file: " + storeFile.getName());
		while ((read = reader.read(buffer)) >= 0) {
			storeStub.store(storeFile.getName(), buffer, read, clientID.getID());
		}
		reader.close();
		outputStream.close();
		notifyStoreFinishedStub.notifyStoreFinished(storeFile, storeFile.getName(), clientID.getID());
	}
}
