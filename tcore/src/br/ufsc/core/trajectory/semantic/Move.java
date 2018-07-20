package br.ufsc.core.trajectory.semantic;

import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;

/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Move {
	
	private int moveId;
	private long startTime;
	private long endTime;
	private Stop start;
	private Stop end;
	private int begin;
	private int length;
	
	private List<Attribute> attributes;
	
	public Move(int moveId, Stop start, Stop end, long startTime, long endTime, int begin, int length, TPoint[] points) {
		this(moveId, start, end, startTime, endTime, begin, length, points, 0.0);
	}

	public Move(int moveId, Stop start, Stop end, long startTime, long endTime, int begin, int length, TPoint[] points, double angle) {
		this(moveId, start, end, startTime, endTime, begin, length, points, angle, 0.0);
	}

	public Move(int moveId, Stop start, Stop end, long startTime, long endTime, int begin, int length, TPoint[] points, double angle, double traveledDistance) {
		this(moveId, start, end, startTime, endTime, begin, length, points, angle, 0.0, null);
	}

	public Move(int moveId, Stop start, Stop end, long startTime, long endTime, int begin, int length, TPoint[] points, double angle, double traveledDistance, String streetName) {
		this(moveId, start, end, startTime, endTime, begin, length, points, angle, 0.0, null, -1, -1);
	}

	public Move(int moveId, Stop start, Stop end, long startTime, long endTime, int begin, int length, TPoint[] points, double angle, double traveledDistance, String streetName, Integer user, Integer dimensaoData) {
		this.moveId = moveId;
		this.start = start;
		this.end = end;
		this.startTime = startTime;
		this.endTime = endTime;
		this.begin = begin;
		this.length = length;
		attributes = Arrays.asList(//
				new Attribute(AttributeType.MOVE_POINTS, points)//
				, new Attribute(AttributeType.MOVE_ANGLE, angle)//
				, new Attribute(AttributeType.MOVE_TRAVELLED_DISTANCE, traveledDistance)//
				, new Attribute(AttributeType.MOVE_TRANSPORTATION_MODE, null)//
				, new Attribute(AttributeType.MOVE_ACTIVITY, null)//
				, new Attribute(AttributeType.MOVE_STREET_NAME, streetName)//
				, new Attribute(AttributeType.MOVE_USER, user)//
				, new Attribute(AttributeType.MOVE_DIMENSAO_DATA, dimensaoData));
	}

	public int getMoveId() {
		return moveId;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public Stop getStart() {
		return start;
	}

	public void setStart(Stop start) {
		this.start = start;
	}

	public Stop getEnd() {
		return end;
	}

	public void setEnd(Stop end) {
		this.end = end;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public TPoint[] getPoints() {
		return (TPoint[]) getAttribute(AttributeType.MOVE_POINTS);
	}

	public double getAngle() {
		return (double) getAttribute(AttributeType.MOVE_ANGLE);
	}

	public double getTravelledDistance() {
		return (double) getAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE);
	}

	public String getStreetName() {
		return (String) getAttribute(AttributeType.MOVE_STREET_NAME);
	}

	public String getActivity() {
		return (String) getAttribute(AttributeType.MOVE_ACTIVITY);
	}

	public String getTransportationMode() {
		return (String) getAttribute(AttributeType.MOVE_TRANSPORTATION_MODE);
	}

	public SemanticTrajectory getTrajectory() {
		return (SemanticTrajectory) getAttribute(AttributeType.TRAJECTORY);
	}

	public void setUser(Integer data) {
		setAttribute(AttributeType.MOVE_USER, data);
	}

	public Integer getUser() {
		return (Integer) getAttribute(AttributeType.MOVE_USER);
	}

	public void setDimensaoData(Integer data) {
		setAttribute(AttributeType.MOVE_DIMENSAO_DATA, data);
	}

	public Integer getDimensaoData() {
		return (Integer) getAttribute(AttributeType.MOVE_DIMENSAO_DATA);
	}
	
	public double getDuration() {
		return this.endTime - this.startTime;
	}
	
	public Object getAttribute(AttributeType type) {
		for (Attribute attribute : attributes) {
			if(attribute.getType() == type) {
				return attribute.getValue();
			}
		}
		return null;
	}
	
	public void setAttribute(AttributeType type, Object value) {
		for (Attribute attribute : attributes) {
			if(attribute.getType() == type) {
				attribute.setValue(value);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + moveId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if (moveId != other.moveId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Move (from '%s' to '%s')", this.start == null ? "" : this.start.getStopName(), this.end == null ? "" : this.end.getStopName());
	}
}
