package br.ufsc.core.trajectory;

import br.ufsc.utils.Distance;

final class LatLonGeographicalSemantic extends Semantic<TPoint, Number> {
	LatLonGeographicalSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return distance(a, i, b, j) <= threshold.doubleValue();
	}

	@Override
	public boolean match(TPoint d1, TPoint d2, Number threshold) {
		return distance(d1, d2) <= threshold.doubleValue();
	}

	@Override
	public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
	}

	public double distance(TPoint d1, TPoint d2) {
		return Distance.distFrom(d1, d2);
	}

	public double similarity(TPoint d1, TPoint d2) {
		return 1 / Math.max(1, distance(d1, d2));
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