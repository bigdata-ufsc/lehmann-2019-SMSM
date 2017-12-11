package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS;

/**
 * Swale (Sequence Weighted Alignment model) similarity computor.
 */
public class SWALE extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private double penalty;
	private double reward;

	private LCSS lcss;

	public SWALE(SWALEParameters params) {
		this.lcss = new LCSS(new LCSS.LCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, params.epsilon));
		this.penalty = params.penalty;
		this.reward = params.reward;
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double max = lcss.score(t1, t2);
		return (max * reward//
				/*		*/+ (penalty * (t1.length() + t2.length() - max * 2))//
				/*		*/+ Math.abs(penalty * (t1.length() + t2.length())))
				/ (Math.abs(penalty * (t1.length() + t2.length())) + Math.abs(reward * Math.min(t1.length(), t2.length())));
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1- getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "SWALE";
	}
	
	public static class SWALEParameters {
		private double epsilon = 0;
		private double penalty = 0;
		private double reward = 50;
		public SWALEParameters() {
		}
		public SWALEParameters(double epsilon, double penalty, double reward) {
			this.epsilon = epsilon;
			this.penalty = penalty;
			this.reward = reward;
		}
		
	}
}
