package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class PatelRecord {

	private String tid;
	private int gid;
	private double time;
	private String clazz;
	private double longitude;
	private double latitude;
	private Integer stop;

	public PatelRecord(String tid, int gid, double time, String clazz, double longitude, double latitude, Integer stop) {
		this.tid = tid;
		this.gid = gid;
		this.time = time;
		this.clazz = clazz;
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

	public String getClazz() {
		return clazz;
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
