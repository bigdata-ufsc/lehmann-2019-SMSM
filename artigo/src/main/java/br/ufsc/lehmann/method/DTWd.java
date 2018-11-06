package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class DTWd extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private Semantic<?, Number>[] semantics;

	public DTWd(Semantic<?, Number>... semantics) {
		this.semantics = semantics;
	}

	@Override
	public double getSimilarity(SemanticTrajectory A, SemanticTrajectory B) {
		double distance = distance(A, B);
		return 1 / distance;
	}

	double distance(SemanticTrajectory A, SemanticTrajectory B) {
		SemanticTrajectory p, q;
		if (A.length() >= B.length()) {
			p = A;
			q = B;
		} else {
			p = B;
			q = A;
		}
		// "DTW matrix" in linear space.
		double[][] dtwMatrix = new double[2][p.length() + 1];
		// The absolute size of the warping window (to each side of the main diagonal)
		int w = p.length();

		// Initialization (all elements of the first line are INFINITY, except the 0th, and
		// the same value is given to the first element of the first analyzed line).
		for (int i = 0; i <= p.length(); i++) {
			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
			dtwMatrix[1][i] = Double.POSITIVE_INFINITY;
		}
		dtwMatrix[0][0] = 0;

		// Distance calculation
		for (int i = 1; i <= q.length(); i++) {
			int beg = Math.max(1, i - w);
			int end = Math.min(i + w, p.length());

			int thisI = i % 2;
			int prevI = (i - 1) % 2;

			// Fixing values to this iteration
			dtwMatrix[i % 2][beg - 1] = Double.POSITIVE_INFINITY;

			for (int j = beg; j <= end; j++) {
				double score = 0.0;
				for (int k = 0; k < semantics.length; k++) {
					Semantic<?, Number> semantic = semantics[k];
					// DTW(i,j) = c(i-1,j-1) + min(DTW(i-1,j-1), DTW(i,j-1), DTW(i-1,j)).
					score += Math.pow(semantic.distance(q, i - 1, p, j - 1).doubleValue(), 2);
				}
				dtwMatrix[i % 2][j] = score + Math.min(dtwMatrix[thisI][j - 1], Math.min(dtwMatrix[prevI][j], dtwMatrix[prevI][j - 1]));
			}
		}
		double distance = dtwMatrix[q.length() % 2][p.length()];
		return distance;
	}

}
