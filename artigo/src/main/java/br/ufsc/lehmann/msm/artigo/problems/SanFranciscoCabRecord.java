package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class SanFranciscoCabRecord {

	private int gid;
	private int taxi_id;
	private Timestamp time;
	private int ocupation;
	private double longitude;
	private double latitude;
	private Integer semanticStop;
	private Integer semanticMoveId;
	private int tid;
	private int road;
	private boolean mall;
	private boolean airport;
	private String direction;
	private String region;
	private String route;
	public SanFranciscoCabRecord(int tid, int gid, int taxi_id, Timestamp time, int occupation, double longitude, double latitude,boolean airport, boolean mall, 
			int road,String direction,String region, String route,
			Integer semanticStop, Integer semanticMoveId) {
		this.tid = tid;
		this.gid = gid;
		this.taxi_id = taxi_id;
		this.time = time;
		this.ocupation = occupation;
		this.longitude = longitude;
		this.latitude = latitude;
		this.airport = airport;
		this.mall = mall;
		this.road = road;
		this.direction = direction;
		this.region = region;
		this.route = route;
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
	public int getOcupation() {
		return ocupation;
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
	public int getRoad() {
		return road;
	}
	public boolean isMall() {
		return mall;
	}
	public boolean isAirport() {
		return airport;
	}
	public String getDirection() {
		return direction;
	}
	public String getRegion() {
		return region;
	}
	public String getRoute() {
		return route;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	
}
