package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class ERP<V> extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private Semantic<V, Number> semantic;
	private V g;

	public ERP(V g, Semantic<V, Number> parameters) {
		this.g = g;
		this.semantic = parameters;
	}

	@Override
	public double getSimilarity(SemanticTrajectory r, SemanticTrajectory s) {
		double distance = distance(r, s);
		return 1 - (distance / Math.max(r.length(), s.length()));
	}

	@Override
	public double distance(SemanticTrajectory r, SemanticTrajectory s) {
		double[][] erpMetric = new double[r.length() + 1][s.length() + 1];

		int tam1 = r.length() + 1;
		int tam2 = s.length() + 1;

		Object[] dataArr1 = new Object[tam1 - 1];
		// The edges of the matrix are filled.
		for (int i = 0; i < r.length(); i++) {
			erpMetric[i][0] = Double.POSITIVE_INFINITY;
			dataArr1[i] = semantic.getData(r, i);
		}
		Object[] dataArr2 = new Object[tam2 - 1];
		for (int i = 0; i < s.length(); i++) {
			erpMetric[0][i] = Double.POSITIVE_INFINITY;
			dataArr2[i] = semantic.getData(s, i);
		}

		// The (0,0) position of the matrix is filled
		erpMetric[0][0] = 0;

		// The rest of the matrix is filled.
		for (int i = 1; i < tam1; i++) {
			for (int j = 1; j < tam2; j++) {
				V data1 = (V) dataArr1[i - 1];
				V data2 = (V) dataArr2[j - 1];
				double dist1 = semantic.distance(data1, g); // Cost if y[i-1] is a gap
				double dist2 = semantic.distance(data2, g); // Cost if x[i-1] is a gap
				double dist12 = semantic.distance(data1, data2); // Cost if no gaps are left.
				erpMetric[i][j] = min(//
						dist1 * dist1 + erpMetric[(i - 1)][j], //
						dist2 * dist2 + erpMetric[i][(j - 1)],//
						dist12 * dist12 + erpMetric[(i - 1)][(j - 1)]//
				);
			}
		}
		double distance = Math.sqrt(erpMetric[r.length()][s.length()]);
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
		return "ERP";
	}

	@Override
	public String name() {
		return "ERP";
	}
}
