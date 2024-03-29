package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
//DTW with linear space
public class DTW<T, V> extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private Semantic<T, V> semantic;
	
	public DTW(Semantic<T, V> semantic) {
		this.semantic = semantic;
	}

	public double getDistance(Trajectory p, Trajectory q) {
		SemanticTrajectory sp = new SemanticTrajectory(p);
		SemanticTrajectory sq = new SemanticTrajectory(q);
		return distance(sp, sq, 1.0);
	}

	public double getSimilarity(SemanticTrajectory p, SemanticTrajectory q) {
		return 1 - distance(p, q, 1.0) / Math.min(p.length(), q.length());
	}
	
	public double distance(SemanticTrajectory A, SemanticTrajectory B) {
		return distance(A, B, 1.0);
	}
	
	public double distance(SemanticTrajectory A, SemanticTrajectory B,double warping) {
		SemanticTrajectory p,q;
		if (A.length()>=B.length()){
			p = A;
			q = B;
		} else {
			p = B;
			q = A;
		}
		
		// "DTW matrix" in linear space.
		double[][] dtwMatrix = new double[2][p.length()+1];
		// The absolute size of the warping window (to each side of the main diagonal)
		int w = (int) Math.ceil(p.length() * warping);

		// Initialization (all elements of the first line are INFINITY, except the 0th, and
		// the same value is given to the first element of the first analyzed line).
		for (int i = 0; i <= p.length(); i++) {
			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
			dtwMatrix[1][i] = Double.POSITIVE_INFINITY;
		}
		dtwMatrix[0][0] = 0;

		// Distance calculation
		for (int i = 1; i <= q.length(); i++) {
			int beg = Math.max(1,i-w);
			int end = Math.min(i+w,p.length());

			int thisI = i % 2;
			int prevI = (i-1) % 2;

			// Fixing values to this iteration
			dtwMatrix[i%2][beg-1] = Double.POSITIVE_INFINITY;

			for (int j = beg; j <= end; j++) {
				// DTW(i,j) = c(i-1,j-1) + min(DTW(i-1,j-1), DTW(i,j-1), DTW(i-1,j)).
				dtwMatrix[i%2][j] = semantic.distance(semantic.getData(q, i-1),semantic.getData(p, j-1))
					+ Math.min(dtwMatrix[thisI][j-1],Math.min(dtwMatrix[prevI][j], dtwMatrix[prevI][j-1]));
			}
		}
		return dtwMatrix[q.length()%2][p.length()];
	}

	@Override
	public String parametrization() {
		return "Semantic: " + this.semantic.description();
	}
}
