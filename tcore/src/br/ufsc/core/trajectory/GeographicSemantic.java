package br.ufsc.core.trajectory;

import br.ufsc.utils.Distance;

public final class GeographicSemantic extends Semantic<TPoint, Number> {
	GeographicSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j) <= threshlod.doubleValue();
	}

	@Override
	public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return Distance.euclidean((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
	}

	public double length(SemanticTrajectory trajectory) {
		double ret = 0;
		for (int i = 0; i < trajectory.length() - 2; i++) {
			ret += distance(trajectory, i, trajectory, i + 1);
		}
		return ret;
	}
}