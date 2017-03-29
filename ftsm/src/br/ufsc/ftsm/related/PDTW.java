package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;
//PrunedDTW based on author's implementation
public class PDTW extends TrajectorySimilarityCalculator<Trajectory> {
	public double getDistance(Trajectory p, Trajectory q) {
		return getDistance(p, q, 1.0);
	}

	public double getDistance(Trajectory A, Trajectory B, double warping) {
		
		Trajectory p,q;
		if (A.length()>=B.length()){
			p = B;
			q = A;
		} else {
			p = A;
			q = B;
		}

		// "DTW matrix" in linear space.
		//System.out.println(p.length()+1);
		double[][] dtwMatrix= new double[2][q.length()+1];
		// The absolute size of the warping window (to each side of the main diagonal)
		int w = (int) Math.ceil(p.length() * warping);

		// Start column
		int sc = 1;
		boolean found_lower;

		// End column
		int ec = 1;
		int ec_next = 0;

		// Upper bound, calculated by the Euclidean distance
		double[] UB = new double[q.length()+1];

		// Calculating UB and initializing DTW matrix
		UB[q.length()] = 0;

		for (int i = q.length()-1; i >= 0; i--) {
			//System.out.println(i);
			
			if (i>=p.length()){
				UB[i] = UB[i+1] + (Distance.euclidean(
						p.getPoint(p.length()-1),q.getPoint(i)));
			} else {			
				UB[i] = UB[i+1] + (Distance.euclidean(
						p.getPoint(i),q.getPoint(i)));
			}

			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
			dtwMatrix[1][i] = Double.POSITIVE_INFINITY;

		}
		

	    
		dtwMatrix[0][q.length()] = Double.POSITIVE_INFINITY;
		dtwMatrix[1][q.length()] = Double.POSITIVE_INFINITY;
		dtwMatrix[0][0] = 0;

		// Upper bound, calculated by the Euclidean distance (for now)
		double ub = UB[0];
		
		//System.out.println("UB[0]="+UB[0]);

		// Distance calculation
		for (int i = 1; i <= q.length(); i++) {

			int thisI = i % 2;
			int prevI = (i-1) % 2;

		//	System.out.println("SC: "+sc+" i: "+i+" w: "+w);
			int beg = (int) Math.max(sc,i-w);
			int end = (int) Math.min(i+w,p.length());
		//	System.out.println("begin: "+beg+" end: "+end);
			
			ub = UB[i-1] + dtwMatrix[prevI][i-1];

			// Fixing values to this iteration
			dtwMatrix[thisI][beg-1] = Double.POSITIVE_INFINITY;
			dtwMatrix[thisI][i] = Double.POSITIVE_INFINITY;
			found_lower = false;

			for (int j = beg; j <= end; j++) {

				// Fixing the case when prune the end and let garbagge in the vector
				if (j > ec) {

					dtwMatrix[thisI][j] = dtwMatrix[thisI][j-1];

				} else {

					dtwMatrix[thisI][j] = Math.min(dtwMatrix[thisI][j-1],Math.min(dtwMatrix[prevI][j],
							dtwMatrix[prevI][j-1]));

				}

				dtwMatrix[thisI][j] += Distance.euclidean(q.getPoint(i-1),p.getPoint(j-1));

				if (dtwMatrix[thisI][j] > ub) {

					if (!found_lower) {
						sc = j + 1;
					}

					if (j > ec) {
						if (j<dtwMatrix[thisI].length-1) {
							dtwMatrix[thisI][j+1] = Double.POSITIVE_INFINITY;
						}
						break; // Prune
					}

				} else {

					found_lower = true;
					ec_next = j;

				}

			}

			// Update information of the last column with value < UB
			ec_next++;
			ec = ec_next;

		}

		return dtwMatrix[q.length()%2][p.length()];

	}


//	double getDistance(Trajectory p, Trajectory q, double warping, double ub) {
//
//		// "DTW matrix" in linear space.
//		double[][] dtwMatrix = new double[2][p.length()+1];
//		// The absolute size of the warping window (to each side of the main diagonal)
//		int w = (int) Math.ceil(p.length() * warping);
//
//		// Start column
//		int sc = 1;
//		boolean found_lower;
//
//		// End column
//		int ec = 1;
//		int ec_next = 1;
//
//		// Initialization (all elements of the first line are INFINITY, except the 0th, and
//		// the same value is given to the first element of the first analyzed line).
//		for (int i = 0; i <= p.length(); i++) {
//			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
//			dtwMatrix[1][i] = Double.POSITIVE_INFINITY;
//		}
//		dtwMatrix[0][0] = 0;
//
//		// Distance calculation
//		for (int i = 1; i <= q.length(); i++) {
//
//			int thisI = i % 2;
//			int prevI = (i-1) % 2;
//
//			int beg = Math.max(sc,i-w);
//			int end = Math.min(i+w,p.length());
//
//			// Fixing values to this iteration
//			dtwMatrix[thisI][beg-1] = Double.POSITIVE_INFINITY;
//			found_lower = false;
//
//			for (int j = beg; j <= end; j++) {
//
//				// Fixing the case when prune the end and let garbagge in the vector
//				if (j > ec) {
//
//					dtwMatrix[thisI][j] = dtwMatrix[thisI][j-1];
//
//				} else {
//
//					dtwMatrix[thisI][j] = Math.min(dtwMatrix[thisI][j-1],Math.min(dtwMatrix[prevI][j],
//							dtwMatrix[prevI][j-1]));
//
//				}
//
//				dtwMatrix[thisI][j] += Distance.euclidean(q.getPoint(i-1),p.getPoint(j-1));
//
//				if (dtwMatrix[thisI][j] > ub) {
//
//					if (!found_lower) {
//						sc = j + 1;
//					}
//
//					if (j > ec) {
//						dtwMatrix[thisI][j+1] = Double.POSITIVE_INFINITY;
//						break; // Prune
//					}
//
//				} else {
//
//					found_lower = true;
//					ec_next = j;
//
//				}
//
//			}
//
//			// Update information of the last column with value < UB
//			ec_next++;
//			ec = ec_next;
//
//		}
//
//		return dtwMatrix[q.length()%2][p.length()];
//
//	}



}

