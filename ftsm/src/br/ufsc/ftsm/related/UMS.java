package br.ufsc.ftsm.related;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.Ellipse;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.util.CreateEllipseMath;
import br.ufsc.utils.Distance;

public class UMS extends TrajectorySimilarityCalculator<Trajectory> {

	public UMS() {

	}

	public double getDistance(ETrajectory E1, ETrajectory E2) {
		int n = E1.trajectoryLength();
		int m = E2.trajectoryLength();

		if (n < 3 || m < 3)
			return 0;

		List<Integer>[] aMatchSet = new ArrayList[n];
		List<Integer>[] bMatchSet = new ArrayList[m];

		double[] shr1 = new double[n];
		double[] shr2 = new double[m];

		for (int i = 0; i < n - 1; i++) {
			Ellipse e1 = E1.getEllipse(i);
			for (int j = 0; j < m - 1; j++) {

				Ellipse e2 = E2.getEllipse(j);
				if (Distance.euclidean(e1.getCenter(), e2.getCenter()) <= e1.getSemiMajorAxis() + e2.getSemiMajorAxis()) {
					double p1shr = getShareness(e1.getF1(), e2);
					if (p1shr>0) {
						if (aMatchSet[i] == null) {

							List<Integer> set = new ArrayList<Integer>();
							set.add(j);
							aMatchSet[i] = set;
						} else {
							aMatchSet[i].add(j);
						}
						if (shr1[i] != 1.0) {
							shr1[i] = p1shr > shr1[i] ? p1shr : shr1[i];
						}
					}

					double p2shr = getShareness(e1.getF2(), e2);
					if (p2shr>0) {
						if (aMatchSet[i + 1] == null) {

							List<Integer> set = new ArrayList<Integer>();
							set.add(j);
							aMatchSet[i + 1] = set;
						} else {
							aMatchSet[i + 1].add(j);
						}
						if (shr1[i + 1] != 1.0) {
							shr1[i + 1] = p2shr > shr1[i + 1] ? p2shr : shr1[i + 1];
						}
					}

					double q1shr = getShareness(e2.getF1(), e1);
					if (q1shr>0) {
						if (bMatchSet[j] == null) {
							List<Integer> set = new ArrayList<Integer>();
							set.add(i);
							bMatchSet[j] = set;
						} else {
							bMatchSet[j].add(i);
						}
						if (shr2[j] != 1.0) {
							shr2[j] = q1shr > shr2[j] ? q1shr : shr2[j];
						}
					}
					
					double q2shr = getShareness(e2.getF2(), e1);

					if (q2shr>0) {
						if (bMatchSet[j + 1] == null) {
							List<Integer> set = new ArrayList<Integer>();
							set.add(i);
							bMatchSet[j + 1] = set;
						} else {
							bMatchSet[j + 1].add(i);
						}
						if (shr2[j + 1] != 1.0) {
							shr2[j + 1] = q2shr > shr2[j + 1] ? q2shr : shr2[j + 1];
						}
					}
				}
			}
		}

		int aContinuity[] = new int[n];
		int bContinuity[] = new int[m];

		double aResult = 0;
		double bResult = 0;

		for (int j = 0; j < n; j++) {
			List<Integer> matchingSet = aMatchSet[j];
			if (j == 0) {
				aContinuity[j] = matchingSet == null ? -1 : Collections.min(aMatchSet[j]);
			} else {
				aContinuity[j] = matchingSet == null ? -1 : getContinuityValue(aContinuity[j - 1], matchingSet);
			}
			if (aContinuity[j] != -1) {
				if (j == 0) {
					aResult++;
				} else if (aContinuity[j] >= aContinuity[j - 1]) {
					aResult++;
				}
			}
		}

		for (int j = 0; j < m; j++) {
			List<Integer> matchingSet = bMatchSet[j];
			if (j == 0) {
				bContinuity[j] = matchingSet == null ? -1 : Collections.min(bMatchSet[j]);
			} else {
				bContinuity[j] = matchingSet == null ? -1 : getContinuityValue(bContinuity[j - 1], matchingSet);
			}

			if (bContinuity[j] != -1) {
				if (j == 0) {
					bResult++;
				} else if (bContinuity[j] >= bContinuity[j - 1]) {
					bResult++;
				}
			}
		}
		
		double continuity = (aResult / n) * (bResult / m);
		
		int sum1 = 0;
		double sum3 = 0;
		for (int j = 0; j < n; j++) {
			if (shr1[j] > 0.0) {
				sum1++;
				sum3 += shr1[j];
			}
		}

		int sum2 = 0;
		double sum4 = 0;
		for (int j = 0; j < m; j++) {
			if (shr2[j] > 0.0) {
				sum2++;
				sum4 += shr2[j];
			}
		}
		double alikeness1 = ((double) sum1 / n);// * bConsistency1;
		double alikeness2 = ((double) sum2 / m);// * aConsistency1;

		double shareness1 = sum3 / n;// * aConsistency1;
		double shareness2 = sum4 / m;// * bConsistency1;

		double alikeness = (alikeness1 * alikeness2);
		double shareness = 0.5 * (shareness1 + shareness2);
		double similarity = (0.5 * (alikeness + shareness)) * continuity;
		return similarity;
	}

	public static int getContinuityValue(double lastValue, List<Integer> matchingList) {
		Collections.sort(matchingList);
		for (Integer i : matchingList) {
			if (i >= lastValue) {
				return i;
			}
		}
		return -1;
	}

	private double getShareness(TPoint p1, Ellipse e) {

		TPoint center = e.getCenter();

		double angle = e.getAngle();
		double cos = Math.cos(angle); // Angle in radians
		double sin = Math.sin(angle);
		
		double cx = center.getX();
		double cy = center.getY();

		double px = p1.getX();
		double py = p1.getY();

		double aCos = (cos * (px - cx) - sin * (py - cy));
		double a = aCos * aCos;
		// double b = Math.pow(sin*(px-cx)+cos*(py-cy),2);
		double bSin = (sin * (px - cx) + cos * (py - cy));
		double b = bSin * bSin;

		double eIn = (a / e.getSemiMinorAxisSquare()) + (b / e.getSemiMajorAxisSquare());

		if (eIn > 1) {
			return 0.0;
		}

	//	double minDist = 1;

		double distF1 = Distance.euclidean(p1, e.getF1());
		double distF2 = Distance.euclidean(p1, e.getF2());
		double distC = Distance.euclidean(p1, center);

		return (1 - (Math.min(Math.min(distF1, distF2), distC) / e.getXi()));
	}
	
	@Override
	public double getSimilarity(Trajectory T1, Trajectory T2) {
		ETrajectory E1 = CreateEllipseMath.createEllipticalTrajectoryFixed(T1);
		ETrajectory E2 = CreateEllipseMath.createEllipticalTrajectoryFixed(T2);
		return getDistance(E1,E2);
	}


}
