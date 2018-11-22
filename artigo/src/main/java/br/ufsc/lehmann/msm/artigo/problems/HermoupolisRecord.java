package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class HermoupolisRecord {

	private Timestamp time;
	private int scenario;
	private int moid;
	private int mpid;
	private int edgeId;
	private double realX;
	private double realY;
	private double relativeTime;
	private String transportationMode;
	private String activity;
	private Integer stopId;
	private Integer moveId;
	private int gid;
	private String sem_traj_tag;

	public HermoupolisRecord(int gid, Timestamp time, int scenario, int moid, int mpid, int edgeId,
			double realX, double realY, double relativeTime, String transportationMode, String activity,
			String sem_traj_tag, Integer stopId, Integer moveId) {
				this.gid = gid;
				this.time = time;
				this.scenario = scenario;
				this.moid = moid;
				this.mpid = mpid;
				this.edgeId = edgeId;
				this.realX = realX;
				this.realY = realY;
				this.relativeTime = relativeTime;
				this.transportationMode = transportationMode;
				this.activity = activity;
				this.sem_traj_tag = sem_traj_tag;
				this.stopId = stopId;
				this.moveId = moveId;

	}

	public Timestamp getTime() {
		return time;
	}

	public int getScenario() {
		return scenario;
	}

	public int getMoid() {
		return moid;
	}

	public int getMpid() {
		return mpid;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public double getRealX() {
		return realX;
	}

	public double getRealY() {
		return realY;
	}

	public double getRelativeTime() {
		return relativeTime;
	}

	public String getTransportationMode() {
		return transportationMode;
	}

	public String getActivity() {
		return activity;
	}

	public Integer getStopId() {
		return stopId;
	}

	public Integer getMoveId() {
		return moveId;
	}

	public int getGid() {
		return gid;
	}

	public String getSem_traj_tag() {
		return sem_traj_tag;
	}
}
