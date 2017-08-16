package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class SergipeTracksRecord {

	private Integer semanticStop;
	private Integer semanticMoveId;
	private int gid;
	private Timestamp time;
	private int tid;
	private double averageSpeed;
	private double traveledDistance;
	private double latitude;
	private double longitude;
	private int rating;
	private int rating_bus;
	private int rating_weather;
	private int car_or_bus;
	private String linha;

	public SergipeTracksRecord(int gid, Timestamp time, int tid, double averageSpeed, double traveledDistance, double latitude, double longitude,
			int rating, int rating_bus, int rating_weather, int car_or_bus, String linha, Integer stop, Integer move) {
		this.gid = gid;
		this.time = time;
		this.tid = tid;
		this.averageSpeed = averageSpeed;
		this.traveledDistance = traveledDistance;
		this.latitude = latitude;
		this.longitude = longitude;
		this.rating = rating;
		this.rating_bus = rating_bus;
		this.rating_weather = rating_weather;
		this.car_or_bus = car_or_bus;
		this.linha = linha;
		this.semanticStop = stop;
		this.semanticMoveId = move;
	}

	public Integer getSemanticStop() {
		return semanticStop;
	}

	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}

	public int getGid() {
		return gid;
	}

	public Timestamp getTime() {
		return time;
	}

	public int getTid() {
		return tid;
	}

	public double getAverageSpeed() {
		return averageSpeed;
	}

	public double getTraveledDistance() {
		return traveledDistance;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getRating() {
		return rating;
	}

	public int getRating_bus() {
		return rating_bus;
	}

	public int getRating_weather() {
		return rating_weather;
	}

	public int getCar_or_bus() {
		return car_or_bus;
	}

	public String getLinha() {
		return linha;
	}
}
