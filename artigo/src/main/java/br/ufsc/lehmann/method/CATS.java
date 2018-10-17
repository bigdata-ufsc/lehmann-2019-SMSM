package br.ufsc.lehmann.method;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;


public class CATS extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private CATSSemanticParameter<TPoint, Number> spatial;
	private CATSSemanticParameter<?, Number> temporal;

	public CATS(CATSSemanticParameter<TPoint, Number> spatial, CATSSemanticParameter<?,Number> temporal) {
		this.spatial = spatial;
		this.temporal = temporal;
	}

	@Override
	public double getSimilarity(SemanticTrajectory r, SemanticTrajectory s) {
		double totalScore = 1.0;

		for (int i = 0; i < r.length(); i++) {
			double subScore = 0;
			for (int j = 0; j < s.length(); j++) {
				if(temporal.semantic.match(r, i, s, j, (Number) temporal.threshlod)) {
					Number distance = spatial.semantic.distance(r, i, s, j);
					double t = ((Number) spatial.threshlod).doubleValue();
					double d = distance.doubleValue();
					if(d <= t) {
						subScore = Math.max(subScore, 1 - (d / t));
					}
				}
			}
			totalScore += subScore;
		}
		return totalScore / r.length();
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1 - getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "CATS";
	}

	public String toString() {
		return "CATS";
	}
	
	@Override
	public String parametrization() {
		return StringUtils.join(new CATSSemanticParameter[] {this.temporal, this.spatial}, "\n");
	}
	
	public static class CATSSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;
		@Override
		public String toString() {
			return "CATSSemanticParameter [semantic=" + semantic.description() + ", threshlod=" + threshlod + "]";
		}
		public CATSSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}
}
