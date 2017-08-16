package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class PisaRecord {

	private Timestamp time;
	private int is_stop;
	private int user_id;
	private int gid;
	private int tid;
	private int daily_tid;
	private double lat;
	private double lon;
	private double ele;
	private double temperature;
	private Integer semanticStopId;
	private Integer semanticMoveId;
	private String weather;
	private String place;
	private String goal;
	private String subGoal;
	private String transportation;
	private String event;
	public PisaRecord(Timestamp time, int is_stop, int user_id, int gid, int tid, int daily_tid, double lat, double lon, double ele,
			double temperature, String weather, String place, String goal, String subGoal,
			String transportation, String event, Integer semanticStopId, Integer semanticMoveId) {
		this.time = time;
		this.is_stop = is_stop;
		this.user_id = user_id;
		this.gid = gid;
		this.tid = tid;
		this.daily_tid = daily_tid;
		this.lat = lat;
		this.lon = lon;
		this.ele = ele;
		this.temperature = temperature;
		this.semanticStopId = semanticStopId;
		this.semanticMoveId = semanticMoveId;
		this.weather = weather;
		this.place = place;
		this.goal = goal;
		this.subGoal = subGoal;
		this.transportation = transportation;
		this.event = event;
	}
	public Timestamp getTime() {
		return time;
	}
	public int getIs_stop() {
		return is_stop;
	}
	public int getUser_id() {
		return user_id;
	}
	public int getGid() {
		return gid;
	}
	public int getTid() {
		return tid;
	}
	public int getDaily_tid() {
		return daily_tid;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public double getEle() {
		return ele;
	}
	public double getTemperature() {
		return temperature;
	}
	public Integer getSemanticStopId() {
		return semanticStopId;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}
	public String getWeather() {
		return weather;
	}
	public String getPlace() {
		return place;
	}
	public String getGoal() {
		return goal;
	}
	public String getSubGoal() {
		return subGoal;
	}
	public String getTransportation() {
		return transportation;
	}
	public String getEvent() {
		return event;
	}

}
