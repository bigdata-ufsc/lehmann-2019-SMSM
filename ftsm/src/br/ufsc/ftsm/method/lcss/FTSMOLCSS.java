package br.ufsc.ftsm.method.lcss;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class FTSMOLCSS extends TrajectorySimilarityCalculator<Trajectory> {

	double threshold;

	public FTSMOLCSS(double threshold) {
		this.threshold = threshold;
	}

	public double getSimilarity(Trajectory R, Trajectory S) {
		// FTSM
		Trajectory T1;
		Trajectory T2;


		if (R.length() <= S.length()) {
			T1 = R;
			T2 = S;
		} else {
			T1 = S;
			T2 = R;
		}

		double dist[] = new double[T1.length()];

		Map<Integer, ArrayDeque<Integer>> M = new HashMap<Integer, ArrayDeque<Integer>>();
		M.put(0, new ArrayDeque<Integer>());
		for (int i = 1; i < T1.length(); i++) {
			dist[i] = dist[i - 1] + Distance.euclidean(T1.getPoint(i), T1.getPoint(i - 1));
			M.put(i, new ArrayDeque<Integer>());
		}

		Queue<NodeLCSSNWBB> queue = new ArrayDeque<>();

		Queue<IntervalLCSSNWBB> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalLCSSNWBB(0, (T2.length() - 1)));

		NodeLCSSNWBB root = new NodeLCSSNWBB(0, (T1.length() / 2), (T1.length() - 1), toCompare);
		//System.out.println("t1: "+T1.length());
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeLCSSNWBB node = queue.poll();

			if (!node.isLeaf) {
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;
				Queue<IntervalLCSSNWBB> matchingList = new ArrayDeque<>();
				//System.out.println("Before Prunnig: "+Arrays.toString(toCompare.toArray(new IntervalLCSS8WBB[toCompare.size()] )));
				for (IntervalLCSSNWBB interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
					int end = -1;

					while (k <= interval.end) {
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= radius) {
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								end = k - 1;
								matchingList.add(new IntervalLCSSNWBB(start, end));
							}
							start = -1;
							end = -1;
						}
						k++;
					}
					if (start != -1) {
						end = k - 1;
						matchingList.add(new IntervalLCSSNWBB(start, end));
					}

				}

				if (!matchingList.isEmpty()) {
					int total = node.end - node.begin;

//					if (total == 1) {
//						queue.add(new NodeLCSS8WBB(node.begin, node.begin, node.begin, matchingList));
//						queue.add(new NodeLCSS8WBB(node.end, node.end, node.end, matchingList));
//					} else
					//System.out.println(total);
					//System.out.println(node.begin);
					if (total <=8) {
						
						for (int i = 0; i<=total; i++){
							int mid = node.begin+i;
							//System.out.println("create node mid: "+mid);
							//ystem.out.println("create node: "+mid+" mid: "+mid + "end: "+mid);
							queue.add(new NodeLCSSNWBB(mid, mid, mid, matchingList));
						}

					} else {
						//System.out.println("ceil: "+Math.ceil(12/8));
						int partitionSize = (total + 8-1)/8;
						//System.out.println(total);
						//System.out.println("partitionSize:"+partitionSize);
						//System.out.println(node.end);
						for (int i = node.begin; i <=node.end;i+=partitionSize){
							int end = Math.min(i+partitionSize, (node.end+1));
							int mid = (i+end)/2;
						//	System.out.println("create node: "+i+" mid: "+mid + "end: "+(end-1));
							//System.out.println("create node: "+i+" mid: "+mid + "end: "+(end-1));
							queue.add(new NodeLCSSNWBB(i, mid, end-1, matchingList));
						}
					}
				}
			} else {
				for (IntervalLCSSNWBB interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {

						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= threshold) {
							M.get(node.mid).add(k + 1);
						}
						k++;
					}
				}
			}
		}

		// Based on the FTSE Algorithm to compute FTSM based on the Matrching Lists
		double result = 0;
		int n2 = T1.length();

		int[] matches = new int[n2 + 1];
		matches[0] = 0;
		int m2 = T2.length();
		for (int i = 1; i < n2 + 1; i++) {
			matches[i] = m2 + 1;
		}
		int max = 0;
		for (int j = 1; j <= n2; j++) {

			int c = 0;
			int temp = matches[0];

			for (Integer k : M.get(j - 1)) {

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

		return result / Math.min(T1.length(), T2.length());

	}
}

class IntervalLCSSNWBB {
	int begin;
	int end;

	public IntervalLCSSNWBB(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		return "["+begin+","+end+"]";
//	}
}

class NodeLCSSNWBB {
	int begin;
	int end;
	int mid;
//	double radius;
	boolean isLeaf;
	Queue<IntervalLCSSNWBB> toCompare;

	public NodeLCSSNWBB(int begin, int mid, int end, Queue<IntervalLCSSNWBB> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		// this.radius = radius;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}