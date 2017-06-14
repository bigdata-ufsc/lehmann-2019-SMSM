package br.ufsc.core.trajectory.semantic;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import br.ufsc.core.trajectory.SemanticTrajectory;
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

	private Set<TPoint> points = new HashSet<TPoint>();
	private TPoint centroid;
	private SemanticTrajectory parent;
	private TPoint startPoint;
	private TPoint endPoint;

	public Stop(int stopId, String stopName, Timestamp startTime, Timestamp endTime, TPoint startPoint, TPoint endPoint, TPoint centroid) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.centroid = centroid;
	}

	public Stop(SemanticTrajectory t, int stopId, Timestamp startTime, Timestamp endTime) {
		this.parent = t;
		this.tid = t.getTrajectoryId();
		this.stopId = stopId;
		this.startTime = startTime;
		this.endTime = endTime;
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

	public Set<TPoint> getPoints() {
		return points;
	}

	public void addPoint(TPoint point) {
		points.add(point);
	}

	public void setCentroid(TPoint p) {
		this.centroid = p;
	}

	public TPoint getCentroid() {
		return centroid;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public SemanticTrajectory getParent() {
		return parent;
	}

	public void setParent(SemanticTrajectory parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		return stopId;
	}

	public TPoint getStartPoint() {
		return startPoint;
	}

	public TPoint getEndPoint() {
		return endPoint;
	}
}
