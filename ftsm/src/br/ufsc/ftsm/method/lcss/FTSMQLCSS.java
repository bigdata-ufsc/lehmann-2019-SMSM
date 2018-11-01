package br.ufsc.ftsm.method.lcss;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;

public class FTSMQLCSS extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private LCSSSemanticParameter param;

	public FTSMQLCSS(LCSSSemanticParameter params){
		this.param = params;
	}

	public double getSimilarity(SemanticTrajectory R, SemanticTrajectory S) {
		int n = R.length();
		int m = S.length();

		// FTSM

		SemanticTrajectory T1;
		SemanticTrajectory T2;

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
			Object iPoint = param.semantic.getData(T1, i);
			Object iPreviousPoint = param.semantic.getData(T1, i - 1);
			dist[i] = dist[i - 1] + param.semantic.distance(iPoint, iPreviousPoint);
		}

		Deque<NodeLCSS4W> queue = new ArrayDeque<>();

		Queue<IntervalLCSS4W> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalLCSS4W(0, (T2.length() - 1)));

		NodeLCSS4W root = new NodeLCSS4W(0, (T1.length() / 2), (T1.length() - 1), toCompare);

		queue.push(root);

		while (!queue.isEmpty()) {
			NodeLCSS4W node = queue.pop();

			if (!node.isLeaf) {
				double threshold = ((Number) param.computeThreshold(node.mid, node.end, T1, T1)).doubleValue();
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;
				ArrayDeque<IntervalLCSS4W> matchingList = new ArrayDeque<>();

				for (IntervalLCSS4W interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
				//int end = -1;

					while (k <= interval.end) {
						Object kPoint = param.semantic.getData(T2, k);
						Object midPoint = param.semantic.getData(T1, node.mid);
						if (param.semantic.distance(kPoint, midPoint) <= radius) {
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
						Object kPoint = param.semantic.getData(T2, k);
						Object midPoint = param.semantic.getData(T1, node.mid);
						double threshold = ((Number) param.computeThreshold(k, node.mid, T2, T1)).doubleValue();

						if (param.semantic.distance(kPoint, midPoint) <= threshold) {
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

	@Override
	public String parametrization() {
		return param.toString();
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

