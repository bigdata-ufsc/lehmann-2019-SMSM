package br.ufsc.lehmann.msm.artigo.clusterers;

import org.apache.commons.math3.ml.clustering.Clusterable;

import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClusterableTrajectory implements Clusterable {
	
	private SemanticTrajectory trajectory;

	public ClusterableTrajectory(SemanticTrajectory trajectory) {
		this.trajectory = trajectory;
	}

	@Override
	public double[] getPoint() {
		throw new UnsupportedOperationException();
	}

	public SemanticTrajectory getTrajectory() {
		return trajectory;
	}

}
