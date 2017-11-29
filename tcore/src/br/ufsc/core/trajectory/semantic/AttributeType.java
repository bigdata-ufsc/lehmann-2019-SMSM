package br.ufsc.core.trajectory.semantic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;

public enum AttributeType {

	MOVE((Move m) -> m),
	MOVE_ANGLE((Move m) -> m.getAngle()),
	MOVE_TRAVELLED_DISTANCE((Move m) -> m.getTravelledDistance()),
	MOVE_DURATION((Move m) -> m.getDuration()),
	MOVE_POINTS((Move m) -> {
		Stop start = m.getStart();
		if(start == null) {
			System.out.println("Move sem start: " + m.getMoveId());
		}
		TPoint lastStartPoint = start.getEndPoint();
		Stop end = m.getEnd();
		TPoint lastEndPoint = end.getStartPoint();
		List<TPoint> points = new ArrayList<>(Arrays.asList(lastStartPoint));
		points.addAll(Arrays.asList(m.getPoints()));
		points.add(lastEndPoint);
		TPoint[] ret = new TPoint[points.size()];
		points.toArray(ret);
		return ret;
	}),
	MOVE_STREET_NAME((Move m) -> m.getStreetName()),
	STOP((Stop s) -> s),
	STOP_CENTROID((Stop s) -> s.getCentroid()),
	STOP_STREET_NAME((Stop s) -> s.getStreetName()),
	STOP_REGION((Stop s) -> s.getRegion()),
	STOP_NAME((Stop s) -> s.getStopName()),
	STOP_STREET_NAME_MOVE_ANGLE((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStreetName();
		} else if(s.getMove() != null) {
			return s.getMove().getAngle();
		}
		return null;
	}),
	STOP_NAME_MOVE_STREET_NAME((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStopName();
		} else if(s.getMove() != null) {
			return s.getMove().getStreetName();
		}
		return null;
	}),
	STOP_TRAFFIC_LIGHT((Stop s) -> s.getTrafficLight()),
	STOP_TRAFFIC_LIGHT_DISTANCE((Stop s) -> s.getTrafficLightDistance()),
	STOP_GEOGRAPHIC((Stop s) -> s.getCentroid()),
	STOP_TEMPORAL((Stop s) -> new TemporalDuration(Instant.ofEpochMilli(s.getStartTime()), Instant.ofEpochMilli(s.getEndTime())));
	
	private Function func;

	private <T, R> AttributeType(java.util.function.Function<T, R> func) {
		this.func = func;
	}

	public <T, R> R getValue(T d1) {
		return (R) func.apply(d1);
	}
}
