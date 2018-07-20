package br.ufsc.core.trajectory.semantic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;

public class AttributeType {

	public static final AttributeType MOVE = new AttributeType((Move m) -> m, "MOVE");
	public static final AttributeType MOVE_ANGLE = new AttributeType((Move m) -> m.getAngle(), "MOVE_ANGLE");
	public static final AttributeType MOVE_TRAVELLED_DISTANCE = new AttributeType((Move m) -> m.getTravelledDistance(), "MOVE_TRAVELLED_DISTANCE");
	public static final AttributeType MOVE_DURATION = new AttributeType((Move m) -> m.getDuration(), "MOVE_DURATION");
	public static final AttributeType TRAJECTORY = new AttributeType((Move m) -> m.getTrajectory(), "TRAJECTORY");
	public static final AttributeType MOVE_POINTS = new AttributeType((Move m) -> {
		Stop start = m.getStart();
		TPoint lastStartPoint = start.getEndPoint();
		Stop end = m.getEnd();
		TPoint lastEndPoint = end.getStartPoint();
		List<TPoint> points = new ArrayList<>(Arrays.asList(lastStartPoint));
		points.addAll(Arrays.asList(m.getPoints()));
		points.add(lastEndPoint);
		TPoint[] ret = new TPoint[points.size()];
		points.toArray(ret);
		return ret;
	}, "MOVE_POINTS");
	public static final AttributeType MOVE_STREET_NAME = new AttributeType((Move m) -> m.getStreetName(), "MOVE_STREET_NAME");
	public static final AttributeType MOVE_USER = new AttributeType((Move m) -> m.getUser(), "MOVE_USER");
	public static final AttributeType MOVE_DIMENSAO_DATA = new AttributeType((Move m) -> m.getDimensaoData(), "MOVE_DIMENSAO_DATA");
	public static final AttributeType MOVE_TRANSPORTATION_MODE = new AttributeType((Move m) -> m.getTransportationMode(), "MOVE_TRANSPORTATION_MODE");
	public static final AttributeType MOVE_ACTIVITY = new AttributeType((Move m) -> m.getActivity(), "MOVE_ACTIVITY");
	public static final AttributeType STOP = new AttributeType((Stop s) -> s, "STOP");
	public static final AttributeType STOP_CENTROID = new AttributeType((Stop s) -> s.getCentroid(), "STOP_CENTROID");
	public static final AttributeType STOP_STREET_NAME = new AttributeType((Stop s) -> s.getStreetName(), "STOP_STREET_NAME");
	public static final AttributeType STOP_REGION = new AttributeType((Stop s) -> s.getRegion(), "STOP_REGION");
	public static final AttributeType STOP_NAME = new AttributeType((Stop s) -> s.getStopName(), "STOP_NAME");
	public static final AttributeType STOP_USER = new AttributeType((Stop s) -> s.getUser(), "STOP_USER");
	public static final AttributeType STOP_DIMENSAO_DATA = new AttributeType((Stop s) -> s.getDimensaoData(), "STOP_DIMENSAO_DATA");
	public static final AttributeType STOP_STREET_NAME_MOVE_ANGLE = new AttributeType((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStreetName();
		} else if(s.getMove() != null) {
			return s.getMove().getAngle();
		}
		return null;
	}, "STOP_STREET_NAME_MOVE_ANGLE");
	public static final AttributeType STOP_NAME_MOVE_STREET_NAME = new AttributeType((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStopName();
		} else if(s.getMove() != null) {
			return s.getMove().getStreetName();
		}
		return null;
	}, "STOP_NAME_MOVE_STREET_NAME");
	public static final AttributeType STOP_TRAFFIC_LIGHT = new AttributeType((Stop s) -> s.getTrafficLight(), "STOP_TRAFFIC_LIGHT");
	public static final AttributeType STOP_TRAFFIC_LIGHT_DISTANCE = new AttributeType((Stop s) -> s.getTrafficLightDistance(), "STOP_TRAFFIC_LIGHT_DISTANCE");
	public static final AttributeType STOP_SPATIAL = new AttributeType((Stop s) -> s.getCentroid(), "STOP_GEOGRAPHIC");
	public static final AttributeType STOP_TEMPORAL = new AttributeType((Stop s) -> new TemporalDuration(Instant.ofEpochMilli(s.getStartTime()), Instant.ofEpochMilli(s.getEndTime())), "STOP_TEMPORAL");
	public static final AttributeType NEXT_MOVE = new AttributeType((Stop s) -> s.getNextMove(), "NEXT_MOVE");
	public static final AttributeType PREVIOUS_MOVE = new AttributeType((Stop s) -> s.getPreviousMove(), "PREVIOUS_MOVE");
	
	private Function func;
	private String name;

	public <T, R> AttributeType(java.util.function.Function<T, R> func, String name) {
		this.func = func;
		this.name = name;
	}

	public <T, R> R getValue(T d1) {
		return (R) func.apply(d1);
	}
	
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "AttributeType [name=" + name + "]";
	}
}
