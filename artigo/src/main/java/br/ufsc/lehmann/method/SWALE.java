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
	private LCSSSemanticParameter[] parameters;

	private LCSS lcss;

	public SWALE(SWALEParameters params) {
		this.lcss = new LCSS(this.parameters);
		this.parameters = params.parameters;
		this.penalty = params.penalty;
		this.reward = params.reward;
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return -1 * distance(t1, t2);
		//double max = lcss.score(t1, t2);
		//return (((max * reward.doubleValue()//
		//		/*		*/+ (penalty.doubleValue() * (t1.length() + t2.length() - max * 2))//
		//		/*		*/+ Math.abs(penalty.doubleValue() * (t1.length() + t2.length())))
		//		/ (Math.abs(penalty.doubleValue() * (t1.length() + t2.length())) + Math.abs(reward.doubleValue() * Math.min(t1.length(), t2.length()))))) / 2;
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		SemanticTrajectory t1B, t2B;
		int len1 = t1.length();
		int len2 = t2.length();
		int slen, glen;

		if (len1 < len2) {
			slen = len1;
			glen = len2;
			t1B = t1;
			t2B = t2;
		} else {
			slen = len2;
			glen = len1;
			t1B = t2;
			t2B = t1;
		}

		double Swale[][] = new double[2][slen + 1];

		// initialization
		Swale[0][0] = 0;
		double p = penalty.doubleValue();
		double r = reward.doubleValue();
		for (int i = 1; i <= slen; i++)
			Swale[0][i] = i * p;

		for (int i = 1; i <= glen; i++) {
			int in = i % 2;
			Swale[in][0] = i * p;
			int im1 = (i - 1) % 2;

			for (int k = 0; k < parameters.length; k++) {
				Object p1 = parameters[k].semantic.getData(t2B, i - 1);
				
				for (int j = 1; j <= slen; j++) {
					Object p2 = parameters[k].semantic.getData(t1B, j - 1);
					if (parameters[k].semantic.match(p1, p2, parameters[k].computeThreshold(p1, p2, t2B, t1B))) {
						Swale[in][j] = r + Swale[im1][j - 1];
					} else {
						Swale[in][j] = p + Math.max(Swale[im1][j], Swale[in][j - 1]);
					}
				}
			}
		}

		int i = glen % 2;

		return Swale[i][slen];
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
