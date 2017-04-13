package br.ufsc.core.trajectory;

import br.ufsc.utils.Distance;

public class SpatialDecaying extends Semantic<TPoint, Number> {

	public SpatialDecaying(int index) {
		super(index);
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance(a, i, b, j, Double.MAX_VALUE);
	}

	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		TPoint p1 = (TPoint) a.getDimensionData(index, i);
		TPoint p2 = (TPoint) b.getDimensionData(index, j);
		double dist = Distance.euclidean(p1, p2);
		if (dist > threshlod.doubleValue())
			return 0;
		else
			return (1 - dist / threshlod.doubleValue());
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j, threshlod).doubleValue() > 0;
	}

}
