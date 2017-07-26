package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class TaxiShangaiRecord {

	private int tid;
	private int gid;
	private Timestamp time;
	private double longitude;
	private double latitude;
	private Integer stop;
	private int taxiId;
	private int passengers;
	private double speed;
	private double angle;
	private Integer move;

	public TaxiShangaiRecord(int tid, int taxiId, int gid, Timestamp time, double longitude, double latitude, int passengers, double speed, double angle, Integer stop, Integer move) {
		this.tid = tid;
		this.taxiId = taxiId;
		this.gid = gid;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.passengers = passengers;
		this.speed = speed;
		this.angle = angle;
		this.stop = stop;
		this.move = move;
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

	public Integer getStop() {
		return stop;
	}

	public int getTaxiId() {
		return taxiId;
	}

	public int getPassengers() {
		return passengers;
	}

	public double getSpeed() {
		return speed;
	}

	public double getAngle() {
		return angle;
	}

	public Integer getMove() {
		return move;
	}
}
