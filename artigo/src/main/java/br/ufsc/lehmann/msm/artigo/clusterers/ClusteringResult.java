package br.ufsc.lehmann.msm.artigo.clusterers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClusteringResult {
	
	private SemanticTrajectory[] trajs;
	private List<List<SemanticTrajectory>> clusteres;
	private int[] clusterLabel;
	private Comparator<SemanticTrajectory> classComparator;

	public ClusteringResult(List<SemanticTrajectory> data, Collection<Collection<SemanticTrajectory>> clusteres, int[] clusterLabel, Comparator<SemanticTrajectory> classComparator) {
		this.classComparator = classComparator;
		this.clusteres = new ArrayList<>(clusteres.size());
		clusteres.forEach((Collection<SemanticTrajectory> trajs) -> {
			this.clusteres.add(new ArrayList<>(trajs));
		});
		this.clusterLabel = clusterLabel;
		this.trajs = data.toArray(new SemanticTrajectory[data.size()]);
	}

	public List<List<SemanticTrajectory>> getClusteres() {
		return clusteres;
	}

	public int[] getClusterLabel() {
		return clusterLabel;
	}

	public double fMeasure() {
		return fMeasure(1);
	}

	public double fMeasure(double beta) {
		int tp = 0, fp = 0, fn = 0;

		for (int i = 0; i < trajs.length - 1; i++) {
			for (int k = i + 1; k < trajs.length; k++) {
				int predI = clusterLabel[i];
				int predJ = clusterLabel[k];
				SemanticTrajectory clsI = trajs[i];
				SemanticTrajectory clsJ = trajs[k];

				if (classComparator.compare(clsI, clsJ) == 0) {
					if (predI == predJ) {
						tp += 1;
					} else {
						fn += 1;
					}
				} else if (predI == predJ) {
					fp += 1;
				}
			}
		}

		double prec = tp / (tp + (double) fp);
		double recall = tp / (tp + (double) fn);

		return ((beta * beta + 1) * prec * recall) / (beta * beta * prec + recall);
	}
}
