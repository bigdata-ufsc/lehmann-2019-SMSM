package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class MTM extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private Semantic<Comparable<? extends Object>, Number> semantic;

	public MTM(Semantic<Comparable<? extends Object>, Number> semantic) {
		this.semantic = semantic;
	}

	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return -1;
	}
	
	public void buildGraph(SemanticTrajectory t1, SemanticTrajectory t2, double threshould) {
		int[][] matrix = new int[t1.length()][t2.length()];
		for (int i = 0; i < t1.length(); i++) {
			for (int j = 0; j < t2.length(); j++) {
				matrix[i][j] = (int) Math.ceil(semantic.distance(t1, i, t2, j).doubleValue());
			}
		}
	}
	
	public boolean precedent(SemanticTrajectory t1, SemanticTrajectory t2, double threshould) {
		return false;
	}
}
