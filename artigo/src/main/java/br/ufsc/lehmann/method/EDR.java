package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class EDR extends TrajectorySimilarityCalculator<SemanticTrajectory>  {

	private EDRSemanticParameter<?, ?>[] parameters;

	public EDR(EDRSemanticParameter<?,?>... parameters) {
		this.parameters = parameters;
	}

	@Override
	public double getSimilarity(SemanticTrajectory r, SemanticTrajectory s) {
		double distance = distance(r, s);
		return 1-(distance / (Math.max(r.length(), s.length()) * parameters.length));
	}

	public double distance(SemanticTrajectory r, SemanticTrajectory s) {
		double[][] edrMetric = new double[r.length() + 1][s.length() + 1];

		for (int i = 0; i <= r.length(); i++) {
			edrMetric[i][0] = i;
		}
		for (int i = 0; i <= s.length(); i++) {
			edrMetric[0][i] = i;
		}

		edrMetric[0][0] = 0;

		for (int i = 1; i <= r.length(); i++) {
			for (int j = 1; j <= s.length(); j++) {
				int subcost = 0;
				for (int k = 0; k < parameters.length; k++) {
					EDRSemanticParameter param = parameters[k];
					if(!param.semantic.match(r, i - 1, s, j - 1, param.threshlod)) {
						subcost += 1;
					}
				}
				edrMetric[i][j] = min(//
						edrMetric[i - 1][j - 1] + subcost,//
						edrMetric[i][j - 1] + parameters.length,//
						edrMetric[i - 1][j] + parameters.length);
			}
		}
		double distance = edrMetric[r.length()][s.length()];
		return distance;
	}

	private double min(double a, double b, double c) {
		if (a <= b && a <= c) {
			return a;
		} else if (b <= c) {
			return b;
		} else {
			return c;
		}
	}

	public String toString() {
		return "EDR";
	}
	
	public static class EDRSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;
		public EDRSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}
}
