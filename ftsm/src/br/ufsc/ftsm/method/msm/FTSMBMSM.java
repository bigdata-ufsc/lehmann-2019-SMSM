package br.ufsc.ftsm.method.msm;

import java.util.ArrayDeque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

//FTSM B-Tree with interval
public class FTSMBMSM extends TrajectorySimilarityCalculator<Trajectory> {

	private double threshold;

	public FTSMBMSM(double threshold) {
		this.threshold = threshold;
	}

	public double getDistance(Trajectory R, Trajectory S) {
		double[] resultT1;
		double[] resultT2;

		int n = R.length();
		int m = S.length();

		Trajectory T1, T2;

		if (n <= m) {
			T1 = R;
			T2 = S;
			resultT1 = new double[n];
			resultT2 = new double[m];
		} else {
			T1 = S;
			T2 = R;
			resultT1 = new double[m];
			resultT2 = new double[n];
			n = T1.length();
			m = T2.length();
		}

		double dist[] = new double[n];

		dist[0] = 0;
		for (int i = 1; i < n; i++) {
			dist[i] = dist[i - 1] + Distance.euclidean(T1.getPoint(i), T1.getPoint(i - 1));
		}

		Queue<NodeBMSM> queue = new ArrayDeque<>();

		Queue<IntervalBMSM> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBMSM(0, (m - 1)));

		NodeBMSM root = new NodeBMSM(0, (n / 2), (n - 1), toCompare);
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeBMSM node = queue.poll();

			if (!node.isLeaf) {
				// prunning step
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;

				Queue<IntervalBMSM> matchingList = new ArrayDeque<>();

				for (IntervalBMSM interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
					//int end = -1;

					while (k <= interval.end) {
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= radius) {
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								//end = k - 1;
								matchingList.add(new IntervalBMSM(start, k-1));
							}
							start = -1;
							//end = -1;
						}
						k++;
					}
					if (start != -1) {
						//end = k - 1;
						matchingList.add(new IntervalBMSM(start, k-1));
					}

				}
				// splitting step
				if (!matchingList.isEmpty()) {

					int mid = (node.begin + node.end) / 2;
					int mid1 = (node.begin + mid) / 2;
					int begin2 = mid + 1;
					int mid2 = (begin2 + node.end) / 2;

					queue.add(new NodeBMSM(node.begin, mid1, mid, matchingList));
					queue.add(new NodeBMSM(begin2, mid2, node.end, matchingList));
				}
			} else {
				// matching step
				for (IntervalBMSM interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= threshold) {
							resultT1[node.mid] = 1;
							resultT2[k] = 1;
						}
						k++;
					}
				}
			}
		}

		double parityAB = 0.0;
		for (int j = 0; j < resultT1.length; j++) {
			parityAB += resultT1[j];
		}

		double parityBA = 0.0;
		for (int j = 0; j < resultT2.length; j++) {
			parityBA += resultT2[j];
		}

		double similarity = (parityAB + parityBA) / (n + m);

		return similarity;

	}

}

class IntervalBMSM {
	int begin;
	int end;

	public IntervalBMSM(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeBMSM {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalBMSM> toCompare;

	public NodeBMSM(int begin, int mid, int end, Queue<IntervalBMSM> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}