package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class Amsterdan2005Record {
	private Timestamp time;
	private int gid;
	private double lat;
	private double lon;
	private String team;
	private int q;
	private String zoneName;
	private String tid;
	private String stopName;
	private Integer semanticStopId;
	private Integer semanticMoveId;
	public Amsterdan2005Record(Timestamp time, int gid, double lat, double lon, String team, int q, String zoneName, String tid, String stopName,
			Integer semanticStopId, Integer semanticMoveId) {
		super();
		this.time = time;
		this.gid = gid;
		this.lat = lat;
		this.lon = lon;
		this.team = team;
		this.q = q;
		this.zoneName = zoneName;
		this.tid = tid;
		this.stopName = stopName;
		this.semanticStopId = semanticStopId;
		this.semanticMoveId = semanticMoveId;
	}
	public Timestamp getTime() {
		return time;
	}
	public int getGid() {
		return gid;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public String getTeam() {
		return team;
	}
	public int getQ() {
		return q;
	}
	public String getZoneName() {
		return zoneName;
	}
	public String getTid() {
		return tid;
	}
	public String getStopName() {
		return stopName;
	}
	public Integer getSemanticStopId() {
		return semanticStopId;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}

}
