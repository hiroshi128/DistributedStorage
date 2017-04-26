package DistributedStorageSystem;

/**
 * This class defines constant variables.
 * @author Hiroshi Arai
 *
 */
public class SystemConstant {
	private SystemConstant() {
	}; // Unable to instantiate

	public static final int RMI_SERVER_PORT = 6134;
	public static final int RMI_STORE_PORT = 6128;
	public static final int RMI_RETRIEVE_PORT = 6129;
	public static final int RMI_PING_PORT = 6130;
	public static final int RMI_DATAMANAGER_PORT = 6131;
	public static final int RMI_DMGRSTUB_PORT = 6132;
	public static final int RMI_REPLICATION_PORT = 6133;
	public static final int RMI_STORE_FINISHED_PORT = 6135;
	public static final int RMI_DELETEFILE_PORT = 6136;

	public static final String DMGR_BIND_NAME = "DMgrComm";
	public static final String STORE_BIND_NAME = "Store";
	public static final String RETRIEVE_BIND_NAME = "Retrieve";
	public static final String PING_BIND_NAME = "CheckConnectionSpeed";
	public static final String REPLICATION_BIND_NAME = "Replication";
	public static final String STOREFINISHED_BIND_NAME = "StoreFinished";
	public static final String DELETEFILE_BIND_NAME ="Delete";

}
