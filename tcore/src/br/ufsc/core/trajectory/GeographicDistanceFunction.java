package br.ufsc.core.trajectory;

public interface GeographicDistanceFunction {

	double distance(TPoint p, TPoint d);
	
	double length(SemanticTrajectory t);

	double convert(double units);
}
