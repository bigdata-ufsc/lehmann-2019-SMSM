package br.ufsc.core.trajectory.semantic;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import br.ufsc.core.trajectory.TPoint;

/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Stop {
	
	private int tid;
	private int stopId;
	private String stopName;
	private Timestamp startTime;
	private Timestamp endTime;
	private double avg;

	private String geom;
	// points interval - indexes
	private int begin, end;
	private Set<TPoint> points = new HashSet<TPoint>();
	private int oid;
	private TPoint centroid;
	
	
	public Stop(int tid, int stopId, String stopName, Timestamp startTime, Timestamp endTime, double avg, int rfId, String rfTableName, int begin, int end, String geom) {
		this.tid = tid;
		this.stopId = stopId;
		this.stopName = stopName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.avg = avg;
		
		this.begin = begin;
		this.end = end;
		this.geom = geom;
	}

	
	public Stop(int tid,int oid,Timestamp startTime,Timestamp endTime) {
		this.tid=tid;
		this.oid=oid;
		this.startTime=startTime;
		this.endTime=endTime;
	}

	public Stop() {
		// TODO Auto-generated constructor stub
	}


	public void setTid(int tid) {
		this.tid = tid;
	}


	public int getTid() {
		return tid;
	}

	public int getStopId() {
		return stopId;
	}

	public String getStopName() {
		return stopName;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public double getAvg() {
		return avg;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}
	
	public String getGeom(){
		return geom;
	}


	public Set<TPoint> getPoints() {
		return points;
	}


	public void addPoint(TPoint point) {
		points.add(point);
	}
	
	public int getOid() {
		return oid;
	}
	
	public void setCentroid(TPoint p){
		this.centroid=p;
	}
	
	public TPoint getCentroid(){
		return centroid;
	}
	
	public void setEndTime(Timestamp endTime){
		this.endTime=endTime;
	}
	
	public void setStartTime(Timestamp startTime){
		this.startTime=startTime;
	}
	
}
