package br.ufsc.db;

public class DBConstants {
	
	public static final int SRID = 4326;

	// Trajectory columns
	public static final String TRAJECTORY_ID = "tid";
	public static final String TRAJECTORY_TIME = "time";
	public static final String TRAJECTORY_GEOM = "geom";
	public static final String TRAJECTORY_X_ALIAS = "x";
	public static final String TRAJECTORY_Y_ALIAS = "y";
	public static final String TRAJECTORY_X_TRANSF_ALIAS = "transfX";
	public static final String TRAJECTORY_Y_TRANSF_ALIAS = "transfY";
	
	// Candidates columns
	public static final String CANDIDATE_ID = "id";
	public static final String CANDIDATE_NAME = "name";
	public static final String CANDIDATE_GEOM = "geom";
	public static final String CANDIDATE_TIME = "time";
	
	// Stop columns
	public static final String STOP_GID = "gid";
	public static final String STOP_TID = "tid";
	public static final String STOP_ID = "stopId";
	public static final String STOP_NAME = "stopName";
	public static final String STOP_START_TIME = "startTime";
	public static final String STOP_END_TIME = "endTime";
	public static final String STOP_AVG = "avgSpeed";
	public static final String STOP_RF_TABLE = "rfTableName";
	public static final String STOP_RF_ID = "rfid";
	public static final String STOP_GEOM = "geom";
	public static final String STOP_BEGIN = "begin";
	public static final String STOPEND = "end";
	
	// Move columns
	public static final String MOVE_TID = "tid";
	public static final String MOVE_ID = "moveId";
	public static final String MOVE_STOP1_ID = "startStopId";
	public static final String MOVE_STOP1_NAME = "startStopName";
	public static final String MOVE_STOP2_ID = "endStopId";
	public static final String MOVE_STOP2_NAME = "endStopName";
	public static final String MOVE_START_TIME = "startTime";
	public static final String MOVE_END_TIME = "endTime";
	public static final String MOVE_BEGIN = "begin";
	public static final String MOVE_END = "end";
}
