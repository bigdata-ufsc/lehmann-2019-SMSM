package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class CaiLee extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private CaiLeeSemanticParameter[] mandatories;
	private CaiLeeSemanticParameter<?, Double>[] parameters;
	private double matchThreshold;

	public CaiLee(CaiLeeSemanticParameter<?, ?>[] mandatories, CaiLeeSemanticParameter<?, Double>[] parameters, double matchThreshold) {
		this.mandatories = mandatories;
		this.parameters = parameters;
		this.matchThreshold = matchThreshold;
	}

	@Override
	public double getDistance(SemanticTrajectory R, SemanticTrajectory S) {
		double[][] CaiLeeMetric = new double[R.length() + 1][S.length() + 1];

		for (int i = 0; i <= R.length(); i++) {
			CaiLeeMetric[i][0] = 0;
		}
		for (int i = 0; i <= S.length(); i++) {
			CaiLeeMetric[0][i] = 0;
		}

		CaiLeeMetric[0][0] = 0;

		for (int i = 1; i <= R.length(); i++) {
			semantic: for (int j = 1; j <= S.length(); j++) {
				for (int k = 0; k < mandatories.length; k++) {
					if(!mandatories[k].semantic.match(R, i - 1, S, j - 1, mandatories[k].threshlod)) {
						CaiLeeMetric[i][j] = Math.max(CaiLeeMetric[i][j - 1], CaiLeeMetric[i - 1][j]);
						continue semantic;
					}
				}
				double matchScore = 0.0;
				for (int k = 0; k < parameters.length; k++) {
					double distance = parameters[k].semantic.distance(R, i - 1, S, j - 1);
					if (distance < parameters[k].threshlod) {
						matchScore += parameters[k].weight;
					}
				}
				if(matchScore > matchThreshold) {
					CaiLeeMetric[i][j] = CaiLeeMetric[i - 1][j - 1] + 1;
				} else {
					CaiLeeMetric[i][j] = Math.max(CaiLeeMetric[i][j - 1], CaiLeeMetric[i - 1][j]);
				}
			}
		}

		double lcs = (double) CaiLeeMetric[R.length()][S.length()];
		double similarity = ((lcs / R.length()) + (lcs / S.length())) / 2;

		return 1 - similarity;
	}

	public static class CaiLeeSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;
		private double weight;

		public CaiLeeSemanticParameter(Semantic<V, T> semantic, T threshlod, double weight) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
			this.weight = weight;
		}
	}
}
