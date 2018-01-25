package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class AmsterdanRecord {

	private Timestamp time;
	private int group_id;
	private int gid;
	private int tid;
	private double lat;
	private double lon;
	private Integer semanticStopId;
	private Integer semanticMoveId;
	public Timestamp getTime() {
		return time;
	}
	public AmsterdanRecord(Timestamp time, int group_id, int gid, int tid, double lat, double lon, Integer semanticStopId, Integer semanticMoveId) {
		super();
		this.time = time;
		this.group_id = group_id;
		this.gid = gid;
		this.tid = tid;
		this.lat = lat;
		this.lon = lon;
		this.semanticStopId = semanticStopId;
		this.semanticMoveId = semanticMoveId;
	}
	public int getGroup_id() {
		return group_id;
	}
	public int getGid() {
		return gid;
	}
	public int getTid() {
		return tid;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public Integer getSemanticStopId() {
		return semanticStopId;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}

}
