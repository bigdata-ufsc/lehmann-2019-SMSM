package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class GeolifeRecord {

	private int tid;
	private int gid;
	private Timestamp time;
	private double longitude;
	private double latitude;
	private int userId;
	private String transportationMode;
	private String POI;
	private Integer semanticStop;
	private Integer semanticMoveId;
	private String path;
	private String direction;
	public GeolifeRecord(int tid, int gid, Timestamp time, double longitude, double latitude, int userId, String transportationMode, String pOI,
			Integer semanticStop, Integer semanticMoveId) {
		this(tid, gid, time, longitude, latitude, userId, transportationMode, pOI, null, null, semanticStop, semanticMoveId);
	}
	
	public GeolifeRecord(int tid, int gid, Timestamp time, double longitude, double latitude, int userId, String transportationMode, String pOI, String path, String direction,
			Integer semanticStop, Integer semanticMoveId) {
		super();
		this.tid = tid;
		this.gid = gid;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.userId = userId;
		this.transportationMode = transportationMode;
		POI = pOI;
		this.direction = direction;
		this.semanticStop = semanticStop;
		this.semanticMoveId = semanticMoveId;
		this.path = path;
	}
	public int getTid() {
		return tid;
	}
	public int getGid() {
		return gid;
	}
	public Timestamp getTime() {
		return time;
	}
	public double getLongitude() {
		return longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public int getUserId() {
		return userId;
	}
	public String getTransportationMode() {
		return transportationMode;
	}
	public String getPOI() {
		return POI;
	}
	public Integer getSemanticStop() {
		return semanticStop;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}
	public String getPath() {
		return path;
	}
	public String getDirection() {
		return direction;
	}
}
