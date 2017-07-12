package br.ufsc.lehmann.msm.artigo.problems;

public class TDriveRecord {

	private String tid;
	private int gid;
	private double time;
	private double longitude;
	private double latitude;
	private Integer stop;
	private Integer semanticMoveId;

	public TDriveRecord(String tid, int gid, double time, double longitude, double latitude, Integer stop, Integer semanticMoveId) {
		this.tid = tid;
		this.gid = gid;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.stop = stop;
		this.semanticMoveId = semanticMoveId;
	}

	public String getTid() {
		return tid;
	}

	public int getGid() {
		return gid;
	}

	public double getTime() {
		return time;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public Integer getStop() {
		return stop;
	}

	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}
}
