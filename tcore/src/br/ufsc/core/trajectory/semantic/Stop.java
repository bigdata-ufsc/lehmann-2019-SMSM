package br.ufsc.core.trajectory.semantic;

import java.util.HashSet;
import java.util.Set;

import br.ufsc.core.trajectory.TPoint;

/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Stop {

	private int stopId;
	private String stopName;
	private long startTime;
	private long endTime;
	private double avg;

	private int begin, length;
	private Set<TPoint> points = new HashSet<TPoint>();
	private TPoint centroid;
	private TPoint startPoint;
	private TPoint endPoint;

	public Stop(int stopId, String stopName, long startTime, long endTime, TPoint startPoint, int beginIndex, TPoint endPoint, int length, TPoint centroid) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.centroid = centroid;
		this.begin = beginIndex;
		this.length = length;
	}

	public Stop(int stopId, int beginIndex, long startTime, int length, long endTime) {
		this.begin = beginIndex;
		this.length = length;
		this.stopId = stopId;
		this.startTime = startTime;
		this.endTime = endTime;
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

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "Stop [stopId=" + stopId + ", stopName=" + stopName + ", startTime=" + startTime + ", endTime=" + endTime + ", avg="
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

	public void setLength(int length) {
		this.length = length;
	}
}
