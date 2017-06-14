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
		Stop aData = d1;
		Stop bData = d2;
		if (aData == null && bData == null) {
			return Double.POSITIVE_INFINITY;
		}
		if (aData == null || bData == null) {
			return Double.POSITIVE_INFINITY;
		}
		if (aData == bData) {
			return 0;
		}
		return distance.distanceInMeters(aData.getCentroid(), bData.getCentroid());
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
