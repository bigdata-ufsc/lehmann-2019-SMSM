package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class SanFranciscoCabRecord {

	private int gid;
	private int taxi_id;
	private Timestamp time;
	private int occupation;
	private double longitude;
	private double latitude;
	private Integer semanticStop;
	private Integer semanticMoveId;
	private int tid;
	public SanFranciscoCabRecord(int tid, int gid, int taxi_id, Timestamp time, int occupation, double longitude, double latitude,
			Integer semanticStop, Integer semanticMoveId) {
		this.tid = tid;
		this.gid = gid;
		this.taxi_id = taxi_id;
		this.time = time;
		this.occupation = occupation;
		this.longitude = longitude;
		this.latitude = latitude;
		this.semanticStop = semanticStop;
		this.semanticMoveId = semanticMoveId;
	}
	public int getGid() {
		return gid;
	}
	public int getTaxiId() {
		return taxi_id;
	}
	public Timestamp getTime() {
		return time;
	}
	public int getOccupation() {
		return occupation;
	}
	public double getLongitude() {
		return longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public Integer getSemanticStop() {
		return semanticStop;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}
	public int getTid() {
		return tid;
	}
	
}
