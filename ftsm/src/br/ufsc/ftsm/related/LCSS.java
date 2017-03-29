package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class LCSS extends TrajectorySimilarityCalculator<Trajectory> {

	private double threshold;

	public LCSS(double spaceThreshold) {
		this.threshold = spaceThreshold;
	}

	public double getDistance(Trajectory R, Trajectory S) {

		int[][] LCSSMetric = new int[R.length() + 1][S.length() + 1];

		for (int i = 0; i <= R.length(); i++) {
			LCSSMetric[i][0] = 0;
		}
		for (int i = 0; i <= S.length(); i++) {
			LCSSMetric[0][i] = 0;
		}

		LCSSMetric[0][0] = 0;

		for (int i = 1; i <= R.length(); i++) {
			for (int j = 1; j <= S.length(); j++) {
				if (Distance.euclidean(R.getPoint(i - 1), S.getPoint(j - 1)) < threshold) {
					LCSSMetric[i][j] = LCSSMetric[i - 1][j - 1] + 1;
				} else {
					LCSSMetric[i][j] = Math.max(LCSSMetric[i][j - 1], LCSSMetric[i - 1][j]);
				}

			}
		}
		
		double result = ((double) LCSSMetric[R.length()][S.length()] / Math.min(R.length(), S.length()));
		
		return result;
	}

}
