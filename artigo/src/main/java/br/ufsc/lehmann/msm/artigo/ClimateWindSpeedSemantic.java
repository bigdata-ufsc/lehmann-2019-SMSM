package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClimateWindSpeedSemantic extends Semantic<Double, Number>{

	private double threshold;

	public ClimateWindSpeedSemantic(int index, double threshold) {
		super(index);
		this.threshold = threshold;
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j).doubleValue() <= threshold;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		Double tempA = (Double) a.getDimensionData(index, i);
		Double tempB = (Double) b.getDimensionData(index, j);
		return Math.abs(tempA - tempB);
	}

}
