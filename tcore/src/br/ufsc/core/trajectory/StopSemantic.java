package br.ufsc.core.trajectory;

import br.ufsc.core.trajectory.semantic.Stop;

public class StopSemantic extends Semantic<Stop, Number> {

	private GeographicDistanceFunction distance;

	public StopSemantic(int index, GeographicDistanceFunction distance) {
		super(index);
		this.distance = distance;
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
			return Double.POSITIVE_INFINITY;
		}
		return distance.distanceInMeters(d1.getCentroid(), d2.getCentroid());
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		double distance = distance(getData(a, i), getData(b, j));
		if (threshold == null) {
			return distance == 0;
		}
		return distance <= threshold.doubleValue();
	}

}
