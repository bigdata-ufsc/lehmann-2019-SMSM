package br.ufsc.core.trajectory;

public interface GeographicDistanceFunction extends IDistanceFunction<TPoint> {

	double length(SemanticTrajectory t);
}
