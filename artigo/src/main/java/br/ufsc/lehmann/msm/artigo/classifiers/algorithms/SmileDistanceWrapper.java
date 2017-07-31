package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import smile.math.distance.Distance;

public class SmileDistanceWrapper<Label> implements Distance<SemanticTrajectory> {

	private IMeasureDistance<SemanticTrajectory> measure;

	public SmileDistanceWrapper(IMeasureDistance<SemanticTrajectory> measure) {
		this.measure = measure;
	}

	@Override
	public double d(SemanticTrajectory x, SemanticTrajectory y) {
		return measure.distance(x, y);
	}
	
}