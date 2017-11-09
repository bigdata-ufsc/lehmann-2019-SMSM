package br.ufsc.ftsm.method.ums;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.Ellipse;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.util.CreateEllipseMath;
import br.ufsc.utils.Distance;

public class FTSMBUMS extends TrajectorySimilarityCalculator<Trajectory> {

	public FTSMBUMS() {

	}

	public double getSimilarity(Trajectory R, Trajectory S) {
		int n = R.length();
		int m = S.length();

		if (n < 3 || m < 3) {
//			if (R.getPoint(0).equals(S.getPoint(0))){
//				return 1;
//			}
			return 0;
		}
		
		ETrajectory E1 = new CreateEllipseMath().createEllipticalTrajectoryFixed(R);
		ETrajectory E2 = new CreateEllipseMath().createEllipticalTrajectoryFixed(S);

		//Initialization
		
		double[] shr1 = new double[n];
		double[] shr2 = new double[m];
		
		int aContinuity[] = new int[n];
		int bContinuity[] = new int[m];

		List<Integer>[] aMatchSet = new ArrayList[n];
		List<Integer>[] bMatchSet = new ArrayList[m];
		
		//FTSM T2 with E1
		double dist[] = new double[E1.length()+1];

		dist[0] = 0;
		dist[1] = E1.getEllipse(0).getMajorAxis();//0;
		for (int i = 1; i < E1.length(); i++) {
			dist[i+1] = dist[i] + E1.getEllipse(i).getMajorAxis();
		}

		Deque<NodeBUMS> queue = new ArrayDeque<>();

		Queue<IntervalBUMS> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBUMS(0, (S.length() - 1)));

		NodeBUMS root = new NodeBUMS(0, (E1.length() / 2), (E1.length() - 1), toCompare);

		queue.push(root);
//System.out.println("dist[]= "+Arrays.toString(dist));
		while (!queue.isEmpty()) {
			NodeBUMS node = queue.pop();

			if (!node.isLeaf) {
				double radius = Math.max(dist[node.mid]-dist[node.begin], dist[node.end+1]-dist[node.mid+1])+E1.getEllipse(node.mid).getSemiMajorAxis();
				
//				System.out.println("select st_buffer(st_makepoint("+E1.getEllipse(node.mid).getCenter().getX()+","+E1.getEllipse(node.mid).getCenter().getY()+"),"+radius+");");
//				System.out.println("$$$ Node Start: "+node.begin+" Node End: "+node.end+" Node Mid: "+node.mid);
//				System.out.println("$$$ Node Start: "+dist[node.begin]+" Node End: "+dist[node.end]+" Node Mid: "+dist[node.mid]);

				Queue<IntervalBUMS> matchingList = new ArrayDeque<>();

				for (IntervalBUMS interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
					//int end = -1;

					while (k <= interval.end) {
						if (Distance.euclidean(S.getPoint(k), E1.getEllipse(node.mid).getCenter()) <= radius) {
							//System.out.println(k);
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								//end = k - 1;
								matchingList.add(new IntervalBUMS(start, k-1));
							}
							start = -1;
							//end = -1;
						}
						k++;
					}
					if (start != -1) {
						//end = k - 1;
						matchingList.add(new IntervalBUMS(start, k-1));
					}

				}

				if (!matchingList.isEmpty()) {

					int n2 = node.begin + node.end;
					int mid = n2 / 2;

					int mid1 = (node.begin + mid) / 2;

					int begin2 = mid + 1;
					int mid2 = (begin2 + node.end) / 2;

					queue.push(new NodeBUMS(begin2, mid2, node.end, matchingList));
					queue.push(new NodeBUMS(node.begin, mid1, mid, matchingList));
				}
			} else {
				//System.out.println("Ellipse: "+node.mid+" ###############");
				for (IntervalBUMS interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {
							double shr = getShareness(S.getPoint(k), E1.getEllipse(node.mid));
							if (shr>0) {
								if (bMatchSet[k] == null) {

									List<Integer> set = new ArrayList<Integer>();
									set.add(node.mid);
									bMatchSet[k] = set;
								} else {
									bMatchSet[k].add(node.mid);
								}
								if (shr2[k] != 1.0) {
									shr2[k] = shr > shr2[k] ? shr : shr2[k];
								}
							}
						k++;
					}
				}
			}
		}
		
		int sum2 = 0;
		double sum4 = 0;
		for (int j = 0; j < m; j++) {
			if (shr2[j] > 0.0) {
				sum2 += 1;
				sum4 += shr2[j];
			}
		}

		if (sum2==0){
			return 0;
		}

		//FTSM T2 with E1
//		dist = new double[E2.length()];
//
//		majorAxis = new double[E2.length()];
//
//		dist[0] =  E2.getEllipse(0).getMajorAxis();//0;
//		majorAxis[0] = E2.getEllipse(0).getMajorAxis();
//		for (int i = 1; i < E2.length(); i++) {
//			dist[i] = dist[i - 1] + E2.getEllipse(i).getMajorAxis();
//			majorAxis[i] = E2.getEllipse(i).getMajorAxis();
//					//Distance.triangular(E1.getEllipse(i).getCenter(), E1.getEllipse(i - 1).getCenter());
//		}
		
		dist = new double[E2.length()+1];
//		double majorAxis[] = new double[E1.length()];

		dist[0] = 0;
		dist[1] = E2.getEllipse(0).getMajorAxis();
		for (int i = 1; i < E2.length(); i++) {
			dist[i+1] = dist[i] + E2.getEllipse(i).getMajorAxis();
		}

		queue = new ArrayDeque<>();

		toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBUMS(0, (R.length() - 1)));

		root = new NodeBUMS(0, (E2.length() / 2), (E2.length() - 1), toCompare);

		queue.push(root);

		while (!queue.isEmpty()) {
			NodeBUMS node = queue.pop();

			if (!node.isLeaf) {				
				double radius = Math.max(dist[node.mid]-dist[node.begin], dist[node.end+1]-dist[node.mid+1])+E2.getEllipse(node.mid).getSemiMajorAxis();
				
				Queue<IntervalBUMS> matchingList = new ArrayDeque<>();

				for (IntervalBUMS interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
					//int end = -1;

					while (k <= interval.end) {
						if (Distance.euclidean(R.getPoint(k), E2.getEllipse(node.mid).getCenter()) <= radius) {
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								//end = k - 1;
								matchingList.add(new IntervalBUMS(start, k-1));
							}
							start = -1;
							//end = -1;
						}
						k++;
					}
					if (start != -1) {
						///end = k - 1;
						matchingList.add(new IntervalBUMS(start, k-1));
					}

				}

				if (!matchingList.isEmpty()) {

					int n2 = node.begin + node.end;
					int mid = n2 / 2;

					int mid1 = (node.begin + mid) / 2;

					int begin2 = mid + 1;
					int mid2 = (begin2 + node.end) / 2;

					queue.push(new NodeBUMS(begin2, mid2, node.end, matchingList));
					queue.push(new NodeBUMS(node.begin, mid1, mid, matchingList));
				}
			} else {
				for (IntervalBUMS interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {
							double shr = getShareness(R.getPoint(k), E2.getEllipse(node.mid));
							if (shr>0) {
								if (aMatchSet[k] == null) {

									List<Integer> set = new ArrayList<Integer>();
									set.add(node.mid);
									aMatchSet[k] = set;
								} else {
									aMatchSet[k].add(node.mid);
								}
								if (shr1[k] != 1.0) {
									shr1[k] = shr > shr1[k] ? shr : shr1[k];
								}
							}
					
						k++;
					}
				}
			}
		}

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
				sum1 += 1;
				sum3 += shr1[j];
			}
		}



		double alikeness1 = ((double) sum1 / n);// * bConsistency1;
		double alikeness2 = ((double) sum2 / m);// * aConsistency1;

		double shareness1 = sum3 / n;// * aConsistency1;
		double shareness2 = sum4 / m;// * bConsistency1;

		double alikeness = (alikeness1 * alikeness2);
		double shareness = 0.5 * (shareness1 + shareness2);

		double similarity = (0.5 * (alikeness + shareness)) * continuity;// *
		
//		 System.out.println("sum1: "+sum1);
//		 System.out.println("sum2: "+sum2);
//		 System.out.println("sum3: "+sum3);
//		 System.out.println("sum4: "+sum4);
//		 System.out.println("alk1: "+alikeness1);
//		 System.out.println("alk2: "+alikeness2);
//		 System.out.println("shr1: "+shareness1);
//		 System.out.println(Arrays.toString(shr1));
//		 System.out.println(Arrays.toString(shr2));
//		 System.out.println("cont:"+continuity);
//		 System.out.println(Arrays.toString(aContinuity));
//		 System.out.println(aResult);
//		 System.out.println(Arrays.toString(bContinuity));
//		 System.out.println(bResult);

		return similarity;

	}
		
	private double getShareness(TPoint p1, Ellipse e) {

		TPoint center = e.getCenter();

		double angle = e.getAngle();
		double cos = Math.cos(angle); // Angle in radians
		double sin = Math.sin(angle);

//		double semiMinorAxis = e.getMinorAxis() / 2;
//		double semiMajorAxis = e.getSemiMajorAxis();// / 2;

//		double semiMinorAxisSquare = semiMinorAxis * semiMinorAxis;
//		double semiMajorAxisSquare = semiMajorAxis * semiMajorAxis;

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

	public static int getContinuityValue(double lastValue, List<Integer> matchingList) {
		Collections.sort(matchingList);
		for (Integer i : matchingList) {
			if (i >= lastValue) {
				return i;
			}
		}
		return -1;
	}

}

class IntervalBUMS {
	int begin;
	int end;

	public IntervalBUMS(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeBUMS {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalBUMS> toCompare;

	public NodeBUMS(int begin, int mid, int end, Queue<IntervalBUMS> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}
