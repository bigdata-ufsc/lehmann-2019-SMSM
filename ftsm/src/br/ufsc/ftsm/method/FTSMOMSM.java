package br.ufsc.ftsm.method;

import java.util.ArrayDeque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;

public class FTSMOMSM extends TrajectorySimilarityCalculator<Trajectory> {

	double threshold;

	public FTSMOMSM(double threshold) {
		this.threshold = threshold;
	}

	public double getSimilarity(Trajectory R, Trajectory S) {
		// FTSM
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

		double dist[] = new double[T1.length()];

		for (int i = 1; i < T1.length(); i++) {
			dist[i] = dist[i - 1] + Distance.euclidean(T1.getPoint(i), T1.getPoint(i - 1));
		}

		Queue<NodeOMSM> queue = new ArrayDeque<>();

		Queue<IntervalOMSM> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalOMSM(0, (T2.length() - 1)));

		NodeOMSM root = new NodeOMSM(0, (T1.length() / 2), (T1.length() - 1), toCompare);
		//System.out.println("t1: "+T1.length());
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeOMSM node = queue.poll();

			if (!node.isLeaf) {
				double radius = Math.max(dist[node.mid] - dist[node.begin], (dist[node.end] - dist[node.mid]))
						+ threshold;
				Queue<IntervalOMSM> matchingList = new ArrayDeque<>();
				//System.out.println("Before Prunnig: "+Arrays.toString(toCompare.toArray(new IntervalLCSS8WBB[toCompare.size()] )));
				for (IntervalOMSM interval : node.toCompare) {
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
								matchingList.add(new IntervalOMSM(start, end));
							}
							start = -1;
							end = -1;
						}
						k++;
					}
					if (start != -1) {
						end = k - 1;
						matchingList.add(new IntervalOMSM(start, end));
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
							queue.add(new NodeOMSM(mid, mid, mid, matchingList));
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
							queue.add(new NodeOMSM(i, mid, end-1, matchingList));
						}
					}
				}
			} else {
				for (IntervalOMSM interval : node.toCompare) {
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

		// Based on the FTSE Algorithm to compute FTSM based on the Matrching Lists
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

class IntervalOMSM {
	int begin;
	int end;

	public IntervalOMSM(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		return "["+begin+","+end+"]";
//	}
}

class NodeOMSM {
	int begin;
	int end;
	int mid;
//	double radius;
	boolean isLeaf;
	Queue<IntervalOMSM> toCompare;

	public NodeOMSM(int begin, int mid, int end, Queue<IntervalOMSM> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		// this.radius = radius;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}