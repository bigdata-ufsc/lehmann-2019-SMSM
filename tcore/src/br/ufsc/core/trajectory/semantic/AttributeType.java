package br.ufsc.core.trajectory.semantic;

import java.util.function.Function;

public enum AttributeType {

	MOVE_ANGLE((Move m) -> m.getAngle()),
	MOVE_TRAVELLED_DISTANCE((Move m) -> m.getTravelledDistance()),
	MOVE_POINTS((Move m) -> m.getPoints()),
	STOP_CENTROID((Stop s) -> s.getCentroid()),
	STOP_STREET_NAME((Stop s) -> s.getStreetName());
	
	private Function func;

	private <T, R> AttributeType(java.util.function.Function<T, R> func) {
		this.func = func;
	}

	public <T, R> R getValue(T d1) {
		return (R) func.apply(d1);
	}
}
