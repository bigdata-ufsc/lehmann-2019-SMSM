package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class ERP extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private Semantic<Number, ?> semantic;
	private double g;

	public ERP(double g, Semantic<Number, ?> parameters) {
		this.g = g;
		this.semantic = parameters;
	}

	@Override
	public double getDistance(SemanticTrajectory r, SemanticTrajectory s) {
		double[][] erpMetric = new double[r.length() + 1][s.length() + 1];

		int tam1 = r.length() + 1;
		int tam2 = s.length() + 1;

		// The edges of the matrix are filled.
		for (int i = 0; i <= r.length(); i++) {
			erpMetric[i][0] = Math.abs(g - semantic.getData(r, i).doubleValue());
		}
		for (int i = 0; i <= s.length(); i++) {
			erpMetric[0][i] = Math.abs(g - semantic.getData(s, i).doubleValue());
		}

		// The (0,0) position of the matrix is filled
		erpMetric[0][0] = 0;

		// The rest of the matrix is filled.
		for (int i = 1; i < tam1; i++) {
			for (int j = 1; j < tam2; j++) {
				double dist1 = Math.abs(g - semantic.getData(r, i - 1).doubleValue()); // Cost if y[i-1] is a gap
				double dist2 = Math.abs(g - semantic.getData(s, i - 1).doubleValue()); // Cost if x[i-1] is a gap
				double dist12 = erpMetric[(i - 1)][(j - 1)]; // Cost if no gaps are left.
				erpMetric[i][j] = min(dist1 + erpMetric[(i - 1)][j], dist2 + erpMetric[i][(j - 1)],
						dist12 + erpMetric[(i - 1)][(j - 1)]);
			}
		}
		return 1 - (erpMetric[r.length()][s.length()] / Math.max(r.length(), s.length()));
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
		return "ERP";
	}
}
