package br.ufsc.core.trajectory;

import br.ufsc.core.trajectory.semantic.Stop;

public class StopSemantic extends Semantic<Stop, Number> {

	private GeographicDistanceFunction function;

	public StopSemantic(int index, GeographicDistanceFunction distance) {
		super(index);
		this.function = distance;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance(getData(a, i), getData(b, j));
	}

	@Override
	public double distance(Stop d1, Stop d2) {
		if (d1 == d2) {
			return 0;
		}
		if (d1 == null || d2 == null) {
			return 1;
		}
		return function.distance(d1.getCentroid(), d2.getCentroid());
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return match(getData(a, i), getData(b, j), threshold);
	}

	public boolean match(Stop d1, Stop d2, Number threshold) {
		double distance = distance(d1, d2);
		if (threshold == null) {
			return distance == 0;
		}
		return distance <= function.convert(threshold.doubleValue());
	}

}
