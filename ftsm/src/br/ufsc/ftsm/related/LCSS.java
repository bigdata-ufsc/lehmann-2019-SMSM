package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class LCSS extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private LCSSSemanticParameter[] parameters;

	public LCSS(LCSSSemanticParameter<?, ?>... parameters) {
		this.parameters = parameters;
	}

	public double getDistance(Trajectory A, Trajectory B) {
		return distance(new SemanticTrajectory(A), new SemanticTrajectory(B));
	}

	@Override
	public double getSimilarity(SemanticTrajectory R, SemanticTrajectory S) {
		return similarity(R, S);
	}

	public double distance(SemanticTrajectory R, SemanticTrajectory S) {
		double similarity = similarity(R, S);
		return 1 - similarity;
	}

	private double similarity(SemanticTrajectory R, SemanticTrajectory S) {
		double similarity = score(R, S);
		return similarity / Math.min(R.length(), S.length());
	}

	public double score(SemanticTrajectory R, SemanticTrajectory S) {
		int[][] LCSSMetric = new int[R.length() + 1][S.length() + 1];

		for (int i = 0; i <= R.length(); i++) {
			LCSSMetric[i][0] = 0;
		}
		for (int i = 0; i <= S.length(); i++) {
			LCSSMetric[0][i] = 0;
		}

		LCSSMetric[0][0] = 0;

		for (int i = 1; i <= R.length(); i++) {
			semantic: for (int j = 1; j <= S.length(); j++) {
				for (int k = 0; k < parameters.length; k++) {
					LCSSSemanticParameter p = parameters[k];
					if (!p.semantic.match(R, i - 1, S, j - 1, p.threshold)/* Distance.euclidean(R.getPoint(i - 1), S.getPoint(j - 1)) < threshold */) {
						LCSSMetric[i][j] = Math.max(LCSSMetric[i][j - 1], LCSSMetric[i - 1][j]);
						continue semantic;
					}
				}
				LCSSMetric[i][j] = LCSSMetric[i - 1][j - 1] + 1;
			}
		}

		double similarity = LCSSMetric[R.length()][S.length()];
		return similarity;
	}

	public static class LCSSSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshold;

		public LCSSSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshold = threshlod;
		}
	}
}
