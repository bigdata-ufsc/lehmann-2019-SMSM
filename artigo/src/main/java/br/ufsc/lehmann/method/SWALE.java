package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;

/**
 * Swale (Sequence Weighted Alignment model) similarity computor.
 */
public class SWALE extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private Number penalty;
	private Number reward;

	private LCSS lcss;

	public SWALE(SWALEParameters params) {
		this.lcss = new LCSS(params.parameters);
		this.penalty = params.penalty;
		this.reward = params.reward;
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double max = lcss.score(t1, t2);
		return (max * reward.doubleValue()//
				/*		*/+ (penalty.doubleValue() * (t1.length() + t2.length() - max * 2))//
				/*		*/+ Math.abs(penalty.doubleValue() * (t1.length() + t2.length())))
				/ (Math.abs(penalty.doubleValue() * (t1.length() + t2.length())) + Math.abs(reward.doubleValue() * Math.min(t1.length(), t2.length())));
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
		private LCSSSemanticParameter[] parameters;
		private Number penalty = 0;
		private Number reward = 50;
		public SWALEParameters() {
		}
		public SWALEParameters(Number penalty, Number reward, LCSSSemanticParameter... parameters) {
			this.penalty = penalty;
			this.reward = reward;
			this.parameters = parameters;
		}
	}
	
	@Override
	public String parametrization() {
		return "SWALE (penalty=" + this.penalty + ", reward=" + this.reward + ", \n\tlcss=" + lcss.parametrization() + ")";
	}
}
