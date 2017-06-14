package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class MSM extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private MSMSemanticParameter<?, ?>[] semantics;

	public MSM(MSMSemanticParameter<?, ?>... semantics) {
		this.semantics = semantics;
	}

	public double getDistance(Trajectory A, Trajectory B) {
		return distance(new SemanticTrajectory(A), new SemanticTrajectory(B));
	}

	public double getSimilarity(SemanticTrajectory A, SemanticTrajectory B) {
		int n = A.length();
		int m = B.length();
		double aScore[] = new double[n];
		double bScore[] = new double[m];
		
		double parityAB = 0.0;
		
		for (int i = 0; i < n; i++) {
			double score = 0.0;
			double maxScore = 0.0;
		
			for (int j = 0; j < m; j++) {
				double semanticScore = 0;
				for (int k = 0; k < semantics.length; k++) {
					Semantic semantic = semantics[k].semantic;
					semanticScore += (semantic.match(A, i, B, j, (Object) semantics[k].threshlod) ? 1 : 0) * semantics[k].weight;
				}
				score = semanticScore;
		
				if (score >= maxScore) {
					maxScore = score;
					bScore[j] = maxScore > bScore[j] ? maxScore : bScore[j];
				}
			}
			aScore[i] = maxScore;
			parityAB += maxScore;
		}
		
		double parityBA = 0;
		for (int j = 0; j < m; j++) {
			parityBA += bScore[j];
		}
		
		double similarity = (parityAB + parityBA) / (A.length() + B.length());

		return similarity;
	}

	public double distance(SemanticTrajectory A, SemanticTrajectory B) {
		return 1 - getSimilarity(A, B);
	}
	
	public static class MSMSemanticParameter<V, T> {
		public double weight;
		private Semantic<V, T> semantic;
		private T threshlod;
		public MSMSemanticParameter(Semantic<V, T> semantic, T threshlod, double weight) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
			this.weight = weight;
		}
	}

}
