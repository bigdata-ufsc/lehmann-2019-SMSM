package br.ufsc.ftsm.method.lcss;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class FTSMQLCSS extends TrajectorySimilarityCalculator<Trajectory> {

	double threshold;

	public FTSMQLCSS(double spaceThreshold) {
		this.threshold = spaceThreshold;
	}

	public double getDistance(Trajectory R, Trajectory S) {
		int n = R.length();
		int m = S.length();

		// FTSM

		Trajectory T1;
		Trajectory T2;

		if (n <= m) {
			T1 = R;
			T2 = S;
		} else {
			T1 = S;
			T2 = R;
		}

		//Initialization Based on the FTSE Algorithm to compute FTSM based on the Matrching Lists
		double result = 0;
		int size = T1.length();

		int[] matches = new int[size + 1];
		matches[0] = 0;
		int m2 = T2.length();
		for (int i = 1; i < size + 1; i++) {
			matches[i] = m2 + 1;
		}
		int max = 0;
		
		//FTSM
		double dist[] = new double[T1.length()];

		dist[0] = 0;
		for (int i = 1; i < T1.length(); i++) {
			dist[i] = dist[i - 1] + Distance.euclidean(T1.getPoint(i), T1.getPoint(i - 1));
		}

		Deque<NodeLCSS4W> queue = new ArrayDeque<>();

		Queue<IntervalLCSS4W> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalLCSS4W(0, (T2.length() - 1)));

		NodeLCSS4W root = new NodeLCSS4W(0, (T1.length() / 2), (T1.length() - 1), toCompare);

		queue.push(root);

		while (!queue.isEmpty()) {
			NodeLCSS4W node = queue.pop();

			if (!node.isLeaf) {
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;
				ArrayDeque<IntervalLCSS4W> matchingList = new ArrayDeque<>();

				for (IntervalLCSS4W interval : node.toCompare) {
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
								matchingList.add(new IntervalLCSS4W(start, k-1));
							}
							start = -1;
							//end = -1;
						}

						k++;
					}
					if (start != -1) {
						//end = k - 1;
						matchingList.add(new IntervalLCSS4W(start, k-1));
					}

				}

				if (!matchingList.isEmpty()) {

					int total = node.end - node.begin;

					if (total == 1) {

						queue.push(new NodeLCSS4W(node.end, node.end, node.end, matchingList));
						queue.push(new NodeLCSS4W(node.begin, node.begin, node.begin, matchingList));

					} else if (total == 2) {
						queue.push(new NodeLCSS4W(node.end, node.end, node.end, matchingList));
						queue.push(new NodeLCSS4W(node.mid, node.mid, node.mid, matchingList));
						queue.push(new NodeLCSS4W(node.begin, node.begin, node.begin, matchingList));

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

						queue.push(new NodeLCSS4W(begin4, mid4, node.end, matchingList));
						queue.push(new NodeLCSS4W(begin3, mid3, q3, matchingList));
						queue.push(new NodeLCSS4W(begin2, mid2, q2, matchingList));
						queue.push(new NodeLCSS4W(node.begin, mid1, q1, matchingList));
					}
				}
			} else {
				ArrayDeque<Integer> matchingList = new ArrayDeque<>();
				for (IntervalLCSS4W interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {

						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= threshold) {
							matchingList.add(k+1);
						}
						k++;
					}
				}
				
				//FTSE
				int c = 0;
				int temp = matches[0];
				
				for (Integer k : matchingList) {

					if (temp < k) {
						while (matches[c] < k) {
							c++;
						}
						temp = matches[c];
						//System.out.println(c + " = " +k);
						matches[c] = k;
						if (c > max) {
							max = c;
						}
					}
				}
				result = max;
			}
		}
		
		return result / Math.min(T1.length(), T2.length());

	}
}

class IntervalLCSS4W {
	int begin;
	int end;

	public IntervalLCSS4W(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeLCSS4W {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalLCSS4W> toCompare;

	public NodeLCSS4W(int begin, int mid, int end, Queue<IntervalLCSS4W> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		// this.radius = radius;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}

