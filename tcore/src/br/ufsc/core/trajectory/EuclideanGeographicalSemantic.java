package br.ufsc.core.trajectory;

import br.ufsc.utils.Distance;

final class EuclideanGeographicalSemantic extends Semantic<TPoint, Number> {
	EuclideanGeographicalSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return distance(a, i, b, j) <= threshold.doubleValue();
	}

	@Override
	public boolean match(TPoint d1, TPoint d2, Number threshold) {
		return _distance(d1, d2) <= threshold.doubleValue();
	}

	@Override
	public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return _distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
	}

	public double distance(TPoint d1, TPoint d2) {
		return _distance(d1, d2);
	}

	public double _distance(TPoint d1, TPoint d2) {
		return Distance.euclidean(d1, d2);
	}

	public double similarity(TPoint d1, TPoint d2) {
		return 1 / Math.max(1, _distance(d1, d2));
	}

	public double similarity(TPoint d1, TPoint d2, Number threshold) {
		if(threshold == null) {
			return similarity(d1, d2);
		}
		double distance = distance(d1, d2);
		double t = threshold.doubleValue();
		return 1 / (distance <= t ? 1 : Math.max(1, distance));
	}
}