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
	private long startTime;
	private long endTime;
	private double avg;

	private int begin, end;
	private Set<TPoint> points = new HashSet<TPoint>();
	private TPoint centroid;
	private SemanticTrajectory parent;
	private TPoint startPoint;
	private TPoint endPoint;

	public Stop(int stopId, String stopName, long startTime, long endTime, TPoint startPoint, TPoint endPoint, TPoint centroid) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.centroid = centroid;
	}

	public Stop(int stopId, String stopName, long startTime, long endTime, TPoint startPoint, int beginIndex, TPoint endPoint, int endIndex, TPoint centroid) {
		this(stopId, stopName, startTime, endTime, startPoint, endPoint, centroid);
		begin = beginIndex;
		end = endIndex;
	}

	public Stop(SemanticTrajectory t, int stopId, int beginIndex, long startTime, int endIndex, long endTime) {
		this.parent = t;
		this.begin = beginIndex;
		this.end = endIndex;
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

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
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

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(long startTime) {
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

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Stop [trajectory=" + tid + ", stopId=" + stopId + ", stopName=" + stopName + ", startTime=" + startTime + ", endTime=" + endTime + ", avg="
				+ avg + ", centroid=" + centroid + ", startPoint=" + startPoint + ", endPoint=" + endPoint + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stop other = (Stop) obj;
		if (stopId != other.stopId)
			return false;
		return true;
	}
}
