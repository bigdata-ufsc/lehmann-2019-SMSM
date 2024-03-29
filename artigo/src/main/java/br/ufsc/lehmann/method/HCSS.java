package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class HCSS extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private HCSSSemanticParameter<?, ?> parameter;
	private Semantic<Integer, ?> weightSemantic;

	public HCSS(HCSSSemanticParameter<?, ?> parameters, Semantic<Integer, ?> weightSemantic) {
		this.parameter = parameters;
		this.weightSemantic = weightSemantic;
	}

	@Override
	public double getSimilarity(SemanticTrajectory R, SemanticTrajectory S) {
		return similarity(R, S);
	}

	@Override
	public double distance(SemanticTrajectory R, SemanticTrajectory S) {
		return 1 - similarity(R, S);
	}

	private double similarity(SemanticTrajectory R, SemanticTrajectory S) {
		int[][] HCSSMetric = new int[R.length() + 1][S.length() + 1];

		for (int i = 0; i <= R.length(); i++) {
			HCSSMetric[i][0] = 0;
		}
		for (int i = 0; i <= S.length(); i++) {
			HCSSMetric[0][i] = 0;
		}

		HCSSMetric[0][0] = 0;

		for (int i = 1; i <= R.length(); i++) {
			for (int j = 1; j <= S.length(); j++) {
				HCSSSemanticParameter p = parameter;
				if (p.semantic.match(R, i - 1, S, j - 1, p.threshold)) {
					HCSSMetric[i][j] = HCSSMetric[i - 1][j - 1] + weightSemantic.getData(R, i - 1);
				} else {
					HCSSMetric[i][j] = Math.max(HCSSMetric[i][j - 1], HCSSMetric[i - 1][j]);
				}
			}
		}

		return ((double) HCSSMetric[R.length()][S.length()]) / Math.min(R.length(), S.length());
	}

	@Override
	public String name() {
		return "HCSS";
	}

	public static class HCSSSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshold;

		public HCSSSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshold = threshlod;
		}
	}
}
