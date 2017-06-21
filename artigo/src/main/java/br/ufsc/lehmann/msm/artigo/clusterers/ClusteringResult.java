package br.ufsc.lehmann.msm.artigo.clusterers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClusteringResult {
	
	private List<List<SemanticTrajectory>> clusteres;
	private int[] clusterLabel;

	public ClusteringResult(Collection<Collection<SemanticTrajectory>> clusteres, int[] clusterLabel) {
		this.clusteres = new ArrayList<>(clusteres.size());
		clusteres.forEach((Collection<SemanticTrajectory> trajs) -> {
			this.clusteres.add(new ArrayList<>(trajs));
		});
		this.clusterLabel = clusterLabel;
	}

	public List<List<SemanticTrajectory>> getClusteres() {
		return clusteres;
	}

	public int[] getClusterLabel() {
		return clusterLabel;
	}
}
