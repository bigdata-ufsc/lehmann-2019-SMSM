package br.ufsc.lehmann.msm.artigo.loader;

import java.sql.Timestamp;

import br.ufsc.core.trajectory.TPoint;

public class AISBrestRecord {

	private int gid;
	private int tid;
	private int shipcode;
	private Timestamp date;
	private double heading;
	private double speed;
	private double cog;
	private double rot;
	private TPoint latlon;
	private String shipType;

	public AISBrestRecord(int gid, int tid, int shipcode, Timestamp date, double heading, double speed, double cog,
			double rot, TPoint latlon, String shipType) {
		super();
		this.gid = gid;
		this.tid = tid;
		this.shipcode = shipcode;
		this.date = date;
		this.heading = heading;
		this.speed = speed;
		this.cog = cog;
		this.rot = rot;
		this.latlon = latlon;
		this.shipType = shipType;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public int getShipcode() {
		return shipcode;
	}
	public void setShipcode(int shipcode) {
		this.shipcode = shipcode;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
	public double getHeading() {
		return heading;
	}
	public void setHeading(double heading) {
		this.heading = heading;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getCog() {
		return cog;
	}
	public void setCog(double cog) {
		this.cog = cog;
	}
	public double getRot() {
		return rot;
	}
	public void setRot(double rot) {
		this.rot = rot;
	}
	public TPoint getLatlon() {
		return latlon;
	}
	public void setLatlon(TPoint latlon) {
		this.latlon = latlon;
	}
	public String getShipType() {
		return shipType;
	}
	public void setShipType(String shipType) {
		this.shipType = shipType;
	}
}
