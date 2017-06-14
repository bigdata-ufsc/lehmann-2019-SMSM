package br.ufsc.core.trajectory;

public interface GeographicDistanceFunction {

	double distanceInMeters(TPoint p, TPoint d);
	
	double length(SemanticTrajectory t);
}
