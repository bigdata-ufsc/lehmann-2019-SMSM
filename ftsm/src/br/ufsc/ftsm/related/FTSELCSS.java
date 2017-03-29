package br.ufsc.ftsm.related;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class FTSELCSS extends TrajectorySimilarityCalculator<Trajectory> {

	double threshold;

	public FTSELCSS(double spaceThreshold) {
		this.threshold = spaceThreshold;
	}

	public double getDistance(Trajectory R, Trajectory S) {

		// Discover the Boundaries for the Grid
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;

		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;

		for (TPoint r : R.getPoints()) {
			if (r.getX() > maxX) {
				maxX = r.getX();
			}
			if (r.getY() > maxY) {
				maxY = r.getY();
			}
			if (r.getX() < minX) {
				minX = r.getX();
			}
			if (r.getY() < minY) {
				minY = r.getY();
			}
		}

		for (TPoint s : S.getPoints()) {
			if (s.getX() > maxX) {
				maxX = s.getX();
			}
			if (s.getY() > maxY) {
				maxY = s.getY();
			}
			if (s.getX() < minX) {
				minX = s.getX();
			}
			if (s.getY() < minY) {
				minY = s.getY();
			}
		}

		int cellsX = (int) Math.ceil((maxX - minX) / threshold)+1;
		int cellsY = (int) Math.ceil((maxY - minY) / threshold)+1;

		// Create and Initialize Grid G
		Cell[][] G = new Cell[cellsX][cellsY];
		
		// Index elements of R to the grid
		for (int k = 0; k < R.length(); k++) {
			TPoint r = R.getPoint(k);

			// Discover Center Cell
			int lXCell = (int) ((r.getX() - minX) / threshold);
			int lYCell = (int) ((r.getY() - minY) / threshold);

			// Index to Center and Border Cells
			for (int i = lXCell - 1; i < lXCell + 2; i++) {
				for (int j = lYCell - 1; j < lYCell + 2; j++) {

					if (i >= 0 && j >= 0 && i < G.length && j < G[0].length) {
						if (G[i][j] == null) {
							G[i][j] = new Cell();
						}
						G[i][j].queue.add(k);
					}
				}
			}
		}

		// Initialize Map to Store Intersection Lists of S
		Map<Integer, ArrayDeque<Integer>> L = new HashMap<Integer, ArrayDeque<Integer>>();

		for (int i = 0; i < S.length(); i++) {
			L.put(i, new ArrayDeque<Integer>());
		}

		// Probe the Grid with the Points of S
		for (int l = 0; l < S.length(); l++) {
			TPoint s = S.getPoint(l);

			int xCell = (int) ((s.getX() - minX) / threshold);
			int yCell = (int) ((s.getY() - minY) / threshold);

			// When match, add to the Intersection List
			if (G[xCell][yCell] != null) {
				for (Integer k : G[xCell][yCell].queue) {
					TPoint r = R.getPoint(k);
					if (Distance.euclidean(r, s) <= threshold) {
						L.get(l).add(k + 1);
					}

				}
			}
		}

		// Compute LCSS based on the Intersection Lists
		double result = 0;
		int n2 = S.length();

		int[] matches = new int[n2 + 1];
		matches[0] = 0;
		int m2 = R.length();
		for (int i = 1; i < n2 + 1; i++) {
			matches[i] = m2 + 1;
		}
		int max = 0;
		for (int j = 1; j <= n2; j++) {

			int c = 0;
			int temp = matches[0];

			for (Integer k : L.get(j - 1)) {

				if (temp < k) {
					while (matches[c] < k) {
						c++;
					}
					temp = matches[c];
					matches[c] = k;
					if (c > max) {
						max = c;
					}
				}
			}
			result = max;
		}

		return result / Math.min(S.length(), R.length());

	}
	
	
	public double getEuclidean(Trajectory R, Trajectory S) {
		long euclidean=0;
		// Discover the Boundaries for the Grid
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;

		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;

		for (TPoint r : R.getPoints()) {
			if (r.getX() > maxX) {
				maxX = r.getX();
			}
			if (r.getY() > maxY) {
				maxY = r.getY();
			}
			if (r.getX() < minX) {
				minX = r.getX();
			}
			if (r.getY() < minY) {
				minY = r.getY();
			}
		}

		for (TPoint s : S.getPoints()) {
			if (s.getX() > maxX) {
				maxX = s.getX();
			}
			if (s.getY() > maxY) {
				maxY = s.getY();
			}
			if (s.getX() < minX) {
				minX = s.getX();
			}
			if (s.getY() < minY) {
				minY = s.getY();
			}
		}

		int cellsX = (int) Math.ceil((maxX - minX) / threshold)+1;
		int cellsY = (int) Math.ceil((maxY - minY) / threshold)+1;

		// Create and Initialize Grid G
		Cell[][] G = new Cell[cellsX][cellsY];
		
		// Index elements of R to the grid
		for (int k = 0; k < R.length(); k++) {
			TPoint r = R.getPoint(k);

			// Discover Center Cell
			int lXCell = (int) ((r.getX() - minX) / threshold);
			int lYCell = (int) ((r.getY() - minY) / threshold);

			// Index to Center and Border Cells
			for (int i = lXCell - 1; i < lXCell + 2; i++) {
				for (int j = lYCell - 1; j < lYCell + 2; j++) {

					if (i >= 0 && j >= 0 && i < G.length && j < G[0].length) {
						if (G[i][j] == null) {
							G[i][j] = new Cell();
						}
						G[i][j].queue.add(k);
					}
				}
			}
		}

		// Initialize Map to Store Intersection Lists of S
		Map<Integer, ArrayDeque<Integer>> L = new HashMap<Integer, ArrayDeque<Integer>>();

		for (int i = 0; i < S.length(); i++) {
			L.put(i, new ArrayDeque<Integer>());
		}

		// Probe the Grid with the Points of S
		for (int l = 0; l < S.length(); l++) {
			TPoint s = S.getPoint(l);

			int xCell = (int) ((s.getX() - minX) / threshold);
			int yCell = (int) ((s.getY() - minY) / threshold);

			// When match, add to the Intersection List
			if (G[xCell][yCell] != null) {
				for (Integer k : G[xCell][yCell].queue) {
					TPoint r = R.getPoint(k);
					euclidean++;
					if (Distance.euclidean(r, s) <= threshold) {
						L.get(l).add(k + 1);
					}

				}
			}
		}

		// Compute LCSS based on the Intersection Lists
//		double result = 0;
//		int n2 = S.length();
//
//		int[] matches = new int[n2 + 1];
//		matches[0] = 0;
//		int m2 = R.length();
//		for (int i = 1; i < n2 + 1; i++) {
//			matches[i] = m2 + 1;
//		}
//		int max = 0;
//		for (int j = 1; j <= n2; j++) {
//
//			int c = 0;
//			int temp = matches[0];
//
//			for (Integer k : L.get(j - 1)) {
//
//				if (temp < k) {
//					while (matches[c] < k) {
//						c++;
//					}
//					temp = matches[c];
//					matches[c] = k;
//					if (c > max) {
//						max = c;
//					}
//				}
//			}
//			result = max;
//		}

		return euclidean;//result / Math.min(S.length(), R.length());

	}
}

class Cell {
	Queue<Integer> queue = new ArrayDeque<Integer>();
}
