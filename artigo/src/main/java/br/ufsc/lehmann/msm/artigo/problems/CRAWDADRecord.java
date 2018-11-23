package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class CRAWDADRecord {

	private int gid;
	private Timestamp time;
	private double x;
	private double y;
	private int tid;
	private String group;

	public CRAWDADRecord(int tid, int gid, double x, double y, Timestamp time, String group) {
		super();
		this.gid = gid;
		this.time = time;
		this.x = x;
		this.y = y;
		this.tid = tid;
		this.group = group;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	
	
}
