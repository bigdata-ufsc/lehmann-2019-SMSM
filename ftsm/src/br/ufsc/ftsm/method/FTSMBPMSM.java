package br.ufsc.ftsm.method;

import java.util.ArrayDeque;
import java.util.Queue;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;
//Perfect Radius
public class FTSMBPMSM extends TrajectorySimilarityCalculator<Trajectory> {

	//FTSM as in the Articles Pseudocode

	private double threshold;
	//private int euclidean;

	public FTSMBPMSM(double threshold) {
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
		
		//System.out.println(Arrays.toString(dist));
		
		
		Queue<NodeBPMSM> queue = new ArrayDeque<>();
		
		Queue<IntervalBPMSM> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBPMSM(0, (m - 1)));
		
		NodeBPMSM root = new NodeBPMSM(0, findPivot(0,n-1,dist), (n - 1), toCompare);
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeBPMSM node = queue.poll();
			
			if (!node.isLeaf) {
				double maxDistToMid = 0.0;
				for (int i = node.begin;i<=node.end;i++){
					double distToMid = Distance.euclidean(T1.getPoint(i), T1.getPoint(node.mid));
					if (distToMid>maxDistToMid){
						maxDistToMid=distToMid;
					}
				}
				
				double radius = maxDistToMid + threshold;
				Queue<IntervalBPMSM> matchingList = new ArrayDeque<>();

				for (IntervalBPMSM interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
				

					while (k <= interval.end) {
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= radius) {
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								matchingList.add(new IntervalBPMSM(start, k-1));
							}
							start = -1;
						}
						k++;
					}
					if (start != -1) {
						matchingList.add(new IntervalBPMSM(start, k-1));
					}

				}
								
				if (!matchingList.isEmpty()) {
					int mid1 = findPivot(node.begin,node.mid,dist);

					int begin2 = node.mid + 1;
					int mid2 = findPivot(begin2,node.end,dist);
					
					queue.add(new NodeBPMSM(node.begin, mid1, node.mid, matchingList));
					queue.add(new NodeBPMSM(begin2, mid2, node.end, matchingList));
				}
			} else {
				for (IntervalBPMSM interval : node.toCompare) {
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

		// System.out.println(Arrays.toString(resultT1));
		// System.out.println(Arrays.toString(resultT2));

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
	
	private int findPivot(int begin, int end,double[] dist) {
		if (end-begin<=1){
			return begin;
		}
		
		if (end-begin==2){
			return begin + (end - begin) / 2;
		}
		
		double avg = (dist[end] + dist[begin]) / 2;
		int mid = begin + (end - begin) / 2;

		double diff = Math.abs(avg - dist[mid]);
		double diffPrev = Math.abs(avg - dist[mid - 1]);
		double diffNext = Math.abs(avg - dist[mid + 1]);

		if ((diff < diffPrev && diff < diffNext)||(diff==diffPrev&&diff==diffNext)) {
			return mid;
		}

		while (begin <= end && (end - begin > 2)) {
			mid = begin + (end - begin) / 2;
		//	System.out.println(mid);
			if (avg < dist[mid])
				end = mid - 1;
			else if (avg > dist[mid])
				begin = mid + 1;
			else
				return mid;
		}

		int result = Math.abs(dist[end] - avg) < Math.abs(avg - dist[begin]) ? end : begin;
		result = Math.abs(dist[mid] - avg) <= Math.abs(avg - dist[result]) ? mid : result;
		return result;
	}
	
	public double getEuclidean(Trajectory R, Trajectory S) {
		int euclidean=0;
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
			euclidean++;
			dist[i] = dist[i - 1] + Distance.euclidean(T1.getPoint(i), T1.getPoint(i - 1));
		}
		
		//System.out.println(Arrays.toString(dist));
		
		
		Queue<NodeBPMSM> queue = new ArrayDeque<>();
		
		Queue<IntervalBPMSM> toCompare = new ArrayDeque<>();
		toCompare.add(new IntervalBPMSM(0, (m - 1)));
		
		NodeBPMSM root = new NodeBPMSM(0, findPivot(0,n-1,dist), (n - 1), toCompare);
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeBPMSM node = queue.poll();
			
			if (!node.isLeaf) {
				double maxDistToMid = 0.0;
				for (int i = node.begin;i<=node.end;i++){
					//euclidean++;
					double distToMid = Distance.euclidean(T1.getPoint(i), T1.getPoint(node.mid));
					if (distToMid>maxDistToMid){
						maxDistToMid=distToMid;
					}
				}
				
				double radius = maxDistToMid + threshold;
				Queue<IntervalBPMSM> matchingList = new ArrayDeque<>();

				for (IntervalBPMSM interval : node.toCompare) {
					int k = interval.begin;
					int start = -1;
				

					while (k <= interval.end) {
						euclidean++;
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= radius) {
							if (start == -1) {
								start = k;
							}
						} else {
							if (start != -1) {
								matchingList.add(new IntervalBPMSM(start, k-1));
							}
							start = -1;
						}
						k++;
					}
					if (start != -1) {
						matchingList.add(new IntervalBPMSM(start, k-1));
					}

				}
								
				if (!matchingList.isEmpty()) {
					int mid1 = findPivot(node.begin,node.mid,dist);

					int begin2 = node.mid + 1;
					int mid2 = findPivot(begin2,node.end,dist);
					
					queue.add(new NodeBPMSM(node.begin, mid1, node.mid, matchingList));
					queue.add(new NodeBPMSM(begin2, mid2, node.end, matchingList));
				}
			} else {
				for (IntervalBPMSM interval : node.toCompare) {
					int k = interval.begin;

					while (k <= interval.end) {
						euclidean++;
						if (Distance.euclidean(T2.getPoint(k), T1.getPoint(node.mid)) <= threshold) {
							resultT1[node.mid] = 1;
							resultT2[k] = 1;
						}
						k++;
					}
				}
			}
		}

		// System.out.println(Arrays.toString(resultT1));
		// System.out.println(Arrays.toString(resultT2));



		return euclidean;

	}
	
	
	
}

//TODO intervalos ao invés da lista de pontos
//TODO usar BBox
//TODO experimento para ver a quantidade de cortes

class IntervalBPMSM {
	int begin;
	int end;

	public IntervalBPMSM(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class NodeBPMSM {
	int begin;
	int end;
	int mid;
	boolean isLeaf;
	Queue<IntervalBPMSM> toCompare;

	public NodeBPMSM(int begin, int mid, int end, Queue<IntervalBPMSM> toCompare) {
		this.mid = mid;
		this.begin = begin;
		this.end = end;
		this.toCompare = toCompare;
		isLeaf = end - begin == 0 ? true : false;
	}
}