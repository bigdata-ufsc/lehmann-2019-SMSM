package br.ufsc.lehmann.method;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.ComputableThreshold;
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
		return 1-(distance);
	}

	public double distance(SemanticTrajectory r, SemanticTrajectory s) {
		double distance = edrDistance(r, s);
		return distance / (Math.max(r.length(), s.length()));
	}

	private double edrDistance(SemanticTrajectory r, SemanticTrajectory s) {
		int m = r.length();
		int n = s.length();

//		double[][] edrMetric = new double[2][n + 1];
//		for (int i = 0; i < edrMetric.length; i++) {
//			for (int j = 0; j < edrMetric[i].length; j++) {
//				edrMetric[i][j] = edrMetric[i].length - (j + (m&i));
//			}
//		}
//		edrMetric[edrMetric.length - 1][n] = 0;
//
//		for (int i = m - 1; i >= 0; i--) {
//			int ndx = i & 1;//odd or even
//			semantic: for (int j = n - 1; j >= 0; j--) {
//				int subcost = 0;
//				for (int k = 0; k < parameters.length; k++) {
//					EDRSemanticParameter param = parameters[k];
//					Object rPoint = param.semantic.getData(r, i);
//					Object sPoint = param.semantic.getData(s, j);
//					if(!param.semantic.match(rPoint, sPoint, param.computeThreshold(rPoint, sPoint, r, s))) {
//						subcost = 1;
//						break;
//					}
//				}
//				edrMetric[ndx][j] = min(//
//						edrMetric[1 - ndx][j + 1] + subcost,//
//						edrMetric[ndx][j + 1] + 1,//
//						edrMetric[1 - ndx][j] + 1);
//			}
//		}
//		double distance = edrMetric[0][0];

		double[][] edrMetric = new double[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			edrMetric[i][0] = i;
		}
		for (int i = 0; i <= n; i++) {
			edrMetric[0][i] = i;
		}

		edrMetric[0][0] = 0;
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				int subcost = 0;
				for (int k = 0; k < parameters.length; k++) {
					EDRSemanticParameter param = parameters[k];
					Object rPoint = param.semantic.getData(r, i - 1);
					Object sPoint = param.semantic.getData(s, j - 1);
					if(!param.semantic.match(rPoint, sPoint, param.computeThreshold(rPoint, sPoint, r, s))) {
						subcost = 1;
						break;
					}
				}
				edrMetric[i][j] = min(//
						edrMetric[i - 1][j - 1] + subcost,//
						edrMetric[i][j - 1] + 1,//
						edrMetric[i - 1][j] + 1);
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
	
	@Override
	public String parametrization() {
		return StringUtils.join(parameters, "\n");
	}
	
	public static class EDRSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshold;
		@Override
		public String toString() {
			if(!(threshold instanceof ComputableThreshold)) {
				return "EDRSemanticParameter [semantic=" + semantic.description() + ", threshold=" + threshold + "]";
			}
			return "EDRSemanticParameter [semantic=" + semantic.description() + ", threshold=" + ((ComputableThreshold) threshold).description() + "]";
		}
		public T computeThreshold(V rElement, V sElement, SemanticTrajectory r, SemanticTrajectory s) {
			if(!(threshold instanceof ComputableThreshold)) {
				return threshold;
			}
			return (T) ((ComputableThreshold) threshold).compute(rElement, sElement, r, s, this.semantic);
		}
		public EDRSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshold = threshlod;
		}
	}
}
