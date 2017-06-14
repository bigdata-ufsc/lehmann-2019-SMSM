package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class DTW2 extends TrajectorySimilarityCalculator<Trajectory> {

	public double getSimilarity(Trajectory r, Trajectory s) {
//		if (r.length() > s.length() || r.length() < s.length()) {
//			int i = -1;
//		}
		double[][] dist = new double[r.length() + 1][s.length() + 1];

		// initialize the dynamic programming seeds
		for (int i = 0, j = s.length(); i <= r.length(); ++i) {
			dist[i][j] = Double.MAX_VALUE;
		}
		for (int j = 0, i = r.length(); j <= s.length(); ++j) {
			dist[i][j] = Double.MAX_VALUE;
		}
		dist[r.length()][s.length()] = 0;

		// state transition
		// EuclideanDistanceCalculator pdc = new EuclideanDistanceCalculator();
		for (int i = r.length() - 1; i >= 0; --i) {
			for (int j = s.length() - 1; j >= 0; --j) {
				TPoint rp = r.getPoint(i);
				TPoint sp = s.getPoint(j);
				double edd = Distance.euclidean(rp, sp);
				double temp = edd + Math.min(dist[i + 1][j + 1], Math.min(dist[i + 1][j], dist[i][j + 1]));
				dist[i][j] = temp;
			}
		}

		return dist[0][0] * -1;
	}

	public String toString() {
		return "DTW";
	}

}
