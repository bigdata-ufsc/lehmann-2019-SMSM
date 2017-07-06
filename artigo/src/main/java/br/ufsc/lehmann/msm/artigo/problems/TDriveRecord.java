package br.ufsc.lehmann.msm.artigo.problems;

public class TDriveRecord {

	private String tid;
	private int gid;
	private double time;
	private double longitude;
	private double latitude;
	private Integer stop;

	public TDriveRecord(String tid, int gid, double time, double longitude, double latitude, Integer stop) {
		this.tid = tid;
		this.gid = gid;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.stop = stop;
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
}
