package br.ufsc.lehmann.msm.artigo.problems;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClimateTemperatureSemantic extends Semantic<Double, Number>{

	private double threshold;

	public ClimateTemperatureSemantic(int index, double threshold) {
		super(index);
		this.threshold = threshold;
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j).doubleValue() <= threshold;
	}

	@Override
	public boolean match(Double d1, Double d2, Number threshlod) {
		return distance(d1, d2) <= threshold;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		Double tempA = (Double) getData(a, i);
		Double tempB = (Double) getData(b, j);
		return distance(tempA, tempB);
	}
	
	@Override
	public double distance(Double d1, Double d2) {
		return Math.abs(d1 - d2);
	}

}
