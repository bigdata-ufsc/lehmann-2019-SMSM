package br.ufsc.ftsm.method.lcss;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class FTSMBLCSS extends TrajectorySimilarityCalculator<Trajectory> {

	double threshold;

	public FTSMBLCSS(double spaceThreshold) {
		this.threshold = spaceThreshold;
	}

	public double getSimilarity(Trajectory R, Trajectory S) {
		int n = R.length();
		int m = S.length();

		
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

		Deque<NodeBLCSS> queue = new ArrayDeque<>();

		Queue<IntervalBLCSS> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBLCSS(0, (T2.length() - 1)));

		NodeBLCSS root = new NodeBLCSS(0, (T1.length() / 2), (T1.length() - 1), toCompare);

		queue.push(root);

		while (!queue.isEmpty()) {
			NodeBLCSS node = queue.pop();

			if (!node.isLeaf) {
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;
				Queue<IntervalBLCSS> matchingList = new ArrayDeque<>();

				for (IntervalBLCSS interval : node.toCompare) {
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
							//	end = k - 1;
								matchingList.add(new IntervalBLCSS(start, k-1));
							}
							start = -1;
							//end = -1;
						}
						k++;
					}
					if (start != -1) {
						//end = k - 1;
						matchingList.add(new IntervalBLCSS(start, k-1));
					}

				}

				if (!matchingList.isEmpty()) {

					int n2 = node.begin + node.end;
					int mid = n2 / 2;

					int mid1 = (node.begin + mid) / 2;

					int begin2 = mid + 1;
					int mid2 = (begin2 + node.end) / 2;

					queue.push(new NodeBLCSS(begin2, mid2, node.end, matchingList));
					queue.push(new NodeBLCSS(node.begin, mid1, mid, matchingList));
				}
			} else {
				
				ArrayDeque<Integer> matchingList = new ArrayDeque<>();
				for (IntervalBLCSS interval : node.toCompare) {
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

class IntervalBLCSS {
	int begin;
	int end;

	public IntervalBLCSS(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeBLCSS {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalBLCSS> toCompare;

	public NodeBLCSS(int begin, int mid, int end, Queue<IntervalBLCSS> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}
