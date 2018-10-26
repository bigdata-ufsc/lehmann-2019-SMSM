package br.ufsc.lehmann.msm.artigo.loader;

import br.ufsc.core.trajectory.TPoint;

public class FoursquareRecord {

	private int gid;
	private int tid;
	private int label;
	private String day;
	private int time;
	private String poi;
	private double price;
	private double rating;
	private String weather;
	private TPoint latlon;
	public FoursquareRecord(int gid, int tid, TPoint latlon, int label, String day, int time, String poi, double price, double rating,
			String weather) {
		super();
		this.gid = gid;
		this.tid = tid;
		this.latlon = latlon;
		this.label = label;
		this.day = day;
		this.time = time;
		this.poi = poi;
		this.price = price;
		this.rating = rating;
		this.weather = weather;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int hour) {
		this.time = hour;
	}
	public String getPoi() {
		return poi;
	}
	public void setPoi(String poi) {
		this.poi = poi;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public String getWeather() {
		return weather;
	}
	public void setWeather(String weather) {
		this.weather = weather;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public TPoint getLatlon() {
		return latlon;
	}
	public void setLatlon(TPoint latlon) {
		this.latlon = latlon;
	}

}
