package br.ufsc.ftsm.method.msm;

import java.util.ArrayDeque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

//FTSM with interval and 4W
public class FTSMQMSM extends TrajectorySimilarityCalculator<Trajectory> {

	private double threshold;

	public FTSMQMSM(double threshold) {
		this.threshold = threshold;
	}

	public double getSimilarity(Trajectory R, Trajectory S) {
		double[] resultT1;
		double[] resultT2;

		int n = R.length();
		int m = S.length();

		Trajectory T1;
		Trajectory T2;

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

		Queue<NodeQMSM> queue = new ArrayDeque<>();

		Queue<IntervalQMSM> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalQMSM(0, (m - 1)));

		NodeQMSM root = new NodeQMSM(0, (n / 2), (n - 1), toCompare);
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeQMSM node = queue.poll();

			if (!node.isLeaf) {
				// prunning step
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;

				Queue<IntervalQMSM> matchingList = new ArrayDeque<>();

				for (IntervalQMSM interval : node.toCompare) {
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
								matchingList.add(new IntervalQMSM(start, k-1));
							}
							start = -1;
							//end = -1;
						}
						k++;
					}
					if (start != -1) {
						//end = k - 1;
						matchingList.add(new IntervalQMSM(start, k-1));
					}

				}
				// splitting step
				if (!matchingList.isEmpty()) {

					int total = node.end - node.begin;

					if (total == 1) {

						queue.add(new NodeQMSM(node.begin, node.begin, node.begin, matchingList));
						queue.add(new NodeQMSM(node.end, node.end, node.end, matchingList));

					} else if (total == 2) {
						queue.add(new NodeQMSM(node.begin, node.begin, node.begin, matchingList));
						queue.add(new NodeQMSM(node.mid, node.mid, node.mid, matchingList));
						queue.add(new NodeQMSM(node.end, node.end, node.end, matchingList));

					} else {
						int n2 = node.begin + node.end;
						int q2 = n2 / 2;
						int q1 = (node.begin + q2) / 2;
						int q3 = ((q2 + 1) + node.end) / 2;

						int mid1 = (node.begin + q1) / 2;

						int begin2 = q1 + 1;
						int mid2 = (begin2 + q2) / 2;

						int begin3 = q2 + 1;
						int mid3 = (begin3 + q3) / 2;

						int begin4 = q3 + 1;
						int mid4 = (begin4 + node.end) / 2;

						queue.add(new NodeQMSM(node.begin, mid1, q1, matchingList));
						queue.add(new NodeQMSM(begin2, mid2, q2, matchingList));
						queue.add(new NodeQMSM(begin3, mid3, q3, matchingList));
						queue.add(new NodeQMSM(begin4, mid4, node.end, matchingList));
					}
				}
			} else {
				// matching step
				for (IntervalQMSM interval : node.toCompare) {
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

class IntervalQMSM {
	int begin;
	int end;

	public IntervalQMSM(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeQMSM {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalQMSM> toCompare;

	public NodeQMSM(int begin, int mid, int end, Queue<IntervalQMSM> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}