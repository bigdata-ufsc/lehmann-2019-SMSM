package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;


public class CATS extends TrajectorySimilarityCalculator<SemanticTrajectory>  {

	private CATSSemanticParameter<?, ?>[] parameters;

	public CATS(CATSSemanticParameter<?,?>... parameters) {
		this.parameters = parameters;
	}

	@Override
	public double getSimilarity(SemanticTrajectory r, SemanticTrajectory s) {
		double totalScore = 1.0;

		for (int i = 0; i < r.length(); i++) {
			double subScore = 0;
			for (int j = 0; j < s.length(); j++) {
				for (int k = 0; k < parameters.length; k++) {
					CATSSemanticParameter<?, Number> param = (CATSSemanticParameter<?, Number>) parameters[k];
					Number distance = param.semantic.distance(r, i, s, j);
					if(distance.doubleValue() <= param.threshlod.doubleValue()) {
						subScore = Math.max(subScore, distance.doubleValue());
					}
				}
			}
			totalScore += subScore;
		}
		return totalScore / r.length();
	}

	public String toString() {
		return "CATS";
	}
	
	public static class CATSSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;
		public CATSSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}
}
