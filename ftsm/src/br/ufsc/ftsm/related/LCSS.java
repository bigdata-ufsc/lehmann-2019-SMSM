package br.ufsc.ftsm.related;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.ComputableThreshold;
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
        int m = R.length();
        int n = S.length();
        int[][] LCSSMetric = new int[2][n + 1];

		for (int i = m - 1; i >= 0; i--) {
			int ndx = i & 1;//odd or even
			semantic: for (int j = n - 1; j >= 0; j--) {
				for (int k = 0; k < parameters.length; k++) {
					LCSSSemanticParameter p = parameters[k];
					Object rPoint = p.semantic.getData(R, i);
					Object sPoint = p.semantic.getData(S, j);
					if (!p.semantic.match(rPoint, sPoint, p.computeThreshold(rPoint, sPoint, R, S))) {
						LCSSMetric[ndx][j] = Math.max(LCSSMetric[ndx][j + 1], LCSSMetric[1 - ndx][j]);
						continue semantic;
					}
				}
				LCSSMetric[ndx][j] = LCSSMetric[1 - ndx][j + 1] + 1;
			}
		}

		double similarity = LCSSMetric[0][0];
		return similarity;
	}

	public static class LCSSSemanticParameter<V, T> {
		public final Semantic<V, T> semantic;
		public final T threshold;

		@Override
		public String toString() {
			if(!(threshold instanceof ComputableThreshold)) {
				return "LCSSSemanticParameter [semantic=" + semantic.description() + ", threshold=" + threshold + "]";
			}
			return "LCSSSemanticParameter [semantic=" + semantic.description() + ", threshold=" + ((ComputableThreshold) threshold).description() + "]";
		}
		public T computeThreshold(V rElement, V sElement, SemanticTrajectory r, SemanticTrajectory s) {
			if(!(threshold instanceof ComputableThreshold)) {
				return threshold;
			}
			return (T) ((ComputableThreshold) threshold).compute(rElement, sElement, r, s, this.semantic);
		}

		public LCSSSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshold = threshlod;
		}
	}

	@Override
	public String parametrization() {
		return StringUtils.join(parameters, "\n");
	}
}
