package br.ufsc.core.trajectory;

public interface SpatialDistanceFunction extends IDistanceFunction<TPoint> {

	double length(SemanticTrajectory t);

	TPoint[] convertToMercator(TPoint[] p);
}
