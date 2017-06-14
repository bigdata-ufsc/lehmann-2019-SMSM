package br.ufsc.lehmann.method;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

public class MTM extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private Semantic<?, Number> semantic;
	private int totalTrajectories;
	private Map<Object, MutableInt> stopCounts = new HashMap<>();
	private double timeThreshold;

	public MTM(Semantic<?, Number> semantic, List<SemanticTrajectory> trajs, double timeThreshould) {
		this.semantic = semantic;
		this.timeThreshold = timeThreshould;
		totalTrajectories = trajs.size();
		for (SemanticTrajectory traj : trajs) {
			for (int j = 0; j < traj.length(); j++) {
				Object data = semantic.getData(traj, j);
				MutableInt counter = stopCounts.get(data);
				if(counter == null) {
					stopCounts.put(data, new MutableInt(1));
				} else {
					counter.increment();
				}
			}
		}
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return SimUser(t1, t2);
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return distance(t1, t2) / Math.max(t1.length(), t2.length());
	}

	public double SimUser(SemanticTrajectory S1, SemanticTrajectory S2) {
		return SimSq(S1, S2);
	}
	
	public double SimSq(SemanticTrajectory S1, SemanticTrajectory S2) {
		WhiteBlackTree graph = buildGraph(S1, S2, timeThreshold);
		List<Path> paths = graph.getMaximalPaths();
		return paths.stream().mapToDouble((Path p) -> sg(p)).sum();
	}
	
	public double sg(Path traj) {
		double accumulatedIuf = 0.0;
		int j = 0;
		PathNode node = traj.root;
		for (; node != null; j++) {
			accumulatedIuf += iuf(node.semanticValue);
			node = node.next;
		}
		return Math.pow(2, j - 1) * accumulatedIuf;
	}
	
	public double iuf(Object semanticLocation) {
		return Math.log(totalTrajectories / stopCounts.get(semanticLocation).intValue());
	}

	public WhiteBlackTree buildGraph(SemanticTrajectory t1, SemanticTrajectory t2, double threshold) {
		int[][] matrix = new int[t1.length()][t2.length()];
		for (int i = 0; i < t1.length(); i++) {
			for (int j = 0; j < t2.length(); j++) {
				matrix[i][j] = semantic.distance(t1, i, t2, j).doubleValue() > 0.0 ? 0 : 1;
			}
		}
		List<int[]> decreasedOrdering = new ArrayList<>(t1.length() * t2.length());
		WhiteBlackTree tree = new WhiteBlackTree();
		for (int i = matrix.length - 1; i > -1; i--) {
			for (int j = matrix[i].length - 1; j > -1; j--) {
				if (matrix[i][j] == 1) {
					tree.addEdge(i, semantic.getData(t1, i), j, semantic.getData(t2, j));
					decreasedOrdering.add(new int[] { i, j, 0/* WHITE */ });
				}
			}
		}
//		List<int[]> edges = new ArrayList<>();
		for (int l = 1; l < decreasedOrdering.size(); l++) {// for l from 2 to k
			tree.whiten();/* Mark all nodes WHITE */
//			for (int[] is : decreasedOrdering) {
//				is[2] = 0/* Mark all nodes WHITE */;
//			}
			for (int t = l - 2; t > -1; t--) {// For t from l-1 down to 1
				if(tree.isWhite(decreasedOrdering.get(t)[1])/* if vt is white */) {
					if (precedent(t1, t2, decreasedOrdering, l, t, threshold)) {
						tree.addEdge(l, semantic.getData(t1, decreasedOrdering.get(l)[0]), t, semantic.getData(t2, decreasedOrdering.get(t)[1]));
						tree.blacken(t);
					}
				}
//				if (decreasedOrdering.get(t)[2] == 0/* if vt is white */) {
//					if (precedent(t1, t2, decreasedOrdering, l, t, threshould)) {
//						edges.add(new int[] { l, t });
//						changeToBlack(decreasedOrdering, edges);
//					}
//				}
			}
		}
		return tree;
	}
//
//	private void changeToBlack(List<int[]> decreasedOrdering, List<int[]> edges) {
//		// TODO Auto-generated method stub
//
//	}

	public boolean precedent(SemanticTrajectory P, SemanticTrajectory Q, List<int[]> V, int origin, int dest, double threshold) {
		int i = V.get(origin)[0];
		int i2 = V.get(dest)[0];
		int j = V.get(origin)[1];
		int j2 = V.get(dest)[1];
		if (i < i2 && j < j2) {
			Instant endP = Semantic.TEMPORAL.getData(P, i).getEnd();
			Instant startP = Semantic.TEMPORAL.getData(P, i2).getStart();
			Instant endQ = Semantic.TEMPORAL.getData(Q, j).getEnd();
			Instant startQ = Semantic.TEMPORAL.getData(Q, j2).getStart();
			long travelTimeQ = startQ.toEpochMilli() - endQ.toEpochMilli();
			long travelTimeP = startP.toEpochMilli() - endP.toEpochMilli();
			double c = Math.abs(travelTimeP - travelTimeQ) / Math.max(travelTimeQ, travelTimeP);
			return c <= threshold;
		}
		return false;
	}
	
	@Override
	public String name() {
		return "MTM";
	}

	class WhiteBlackTree {
		Map<Integer, TreeNode> originIndex = new HashMap<>();
		Map<Integer, TreeNode> destinIndex = new HashMap<>();
		List<TreeNode> rootNodes = new ArrayList<>();

		void addEdge(int origin, Object semanticOrigin, int destin, Object semanticDestin) {
			TreeNode o = originIndex.get(origin);
			if(o == null) {
				o = new TreeNode(origin, semanticOrigin);
				originIndex.put(origin, o);
				rootNodes.add(o);
			}
			TreeNode d = destinIndex.get(destin);
			if(d == null) {
				d = new TreeNode(destin, semanticDestin);
				destinIndex.put(destin, d);
			} else if(origin != destin && rootNodes.contains(d)) {
				rootNodes.remove(d);
			}
			TreeEdge edge = new TreeEdge(o, d);
			o.edges.add(edge);
		}

		public List<Path> getMaximalPaths() {
			PathBuilder builder = new PathBuilder();
			List<TreeNode> queue = new ArrayList<>(rootNodes);
			while (!queue.isEmpty()) {
				TreeNode visiting = queue.remove(0);
				visit(visiting, builder);
			}
			List<Path> ret = new ArrayList<>(builder.maximalPaths);
			return ret;
		}

		private void visit(TreeNode visiting, PathBuilder builder) {
			builder.nodes.push(visiting);
			List<TreeEdge> edges = visiting.edges;
			if(edges.isEmpty()) {
				builder.construct();
			} else {
				edges.stream().forEach((TreeEdge edge) -> {
					visit(edge.destin, builder);
				});
			}
			builder.nodes.pop();
		}

		public boolean isWhite(int origin) {
			TreeNode node = originIndex.get(origin);
			return node != null && !node.colorized;
		}

		void blacken() {
			List<TreeNode> queue = new ArrayList<>(rootNodes);
			while (!queue.isEmpty()) {
				TreeNode visiting = queue.remove(0);
				if(!visiting.colorized) {
					visiting.colorized = true;
					List<TreeEdge> edges = visiting.edges;
					queue.addAll(edges.stream().map((TreeEdge edge) -> edge.destin).collect(Collectors.toList()));
				}
			}
		}

		public void blacken(int destin) {
			TreeNode node = destinIndex.get(destin);
			node.colorized = true;
			List<TreeNode> queue = new ArrayList<>(node.edges.stream().map((TreeEdge edge) -> edge.destin).collect(Collectors.toList()));
			while (!queue.isEmpty()) {
				TreeNode visiting = queue.remove(0);
				if(!visiting.colorized) {
					visiting.colorized = true;
					List<TreeEdge> edges = visiting.edges;
					queue.addAll(edges.stream().map((TreeEdge edge) -> edge.destin).collect(Collectors.toList()));
				}
			}
			
		}

		void whiten() {
			List<TreeNode> queue = new ArrayList<>(rootNodes);
			while (!queue.isEmpty()) {
				TreeNode visiting = queue.remove(0);
				if(visiting.colorized) {
					visiting.colorized = false;
					List<TreeEdge> edges = visiting.edges;
					queue.addAll(edges.stream().map((TreeEdge edge) -> edge.destin).collect(Collectors.toList()));
				}
			}
		}
	}

	static class TreeNode {
		int value;
		boolean colorized;
		List<TreeEdge> edges = new ArrayList<>();
		public Object semanticValue;
		public TreeNode(int value, Object semanticValue) {
			this.value = value;
			this.semanticValue = semanticValue;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TreeNode other = (TreeNode) obj;
			if (colorized != other.colorized)
				return false;
			if (semanticValue == null) {
				if (other.semanticValue != null)
					return false;
			} else if (!semanticValue.equals(other.semanticValue))
				return false;
			if (value != other.value)
				return false;
			return true;
		}
	}

	static class TreeEdge {
		TreeNode origin;
		TreeNode destin;
		public TreeEdge(TreeNode origin, TreeNode destin) {
			this.origin = origin;
			this.destin = destin;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TreeEdge other = (TreeEdge) obj;
			if (destin == null) {
				if (other.destin != null)
					return false;
			} else if (destin != (other.destin))
				return false;
			if (origin == null) {
				if (other.origin != null)
					return false;
			} else if (origin != (other.origin))
				return false;
			return true;
		}
	}
	
	static class Path {
		PathNode root;
		PathNode last;
		public Path(TreeNode rootElement) {
			this.root = new PathNode(rootElement);
			this.last = root;
		}
		public void addNode(TreeNode nextElement) {
			last.next = new PathNode(nextElement);
			last = last.next;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Path other = (Path) obj;
			if (last == null) {
				if (other.last != null)
					return false;
			} else if (last != (other.last))
				return false;
			if (root == null) {
				if (other.root != null)
					return false;
			} else if (root != (other.root))
				return false;
			return true;
		}
	}
	
	static class PathNode {
		int value;
		Object semanticValue;
		PathNode next;
		PathNode(TreeNode node) {
			this.value = node.value;
			this.semanticValue = node.semanticValue;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PathNode other = (PathNode) obj;
			if (next == null) {
				if (other.next != null)
					return false;
			} else if (next != (other.next))
				return false;
			if (semanticValue == null) {
				if (other.semanticValue != null)
					return false;
			} else if (!semanticValue.equals(other.semanticValue))
				return false;
			if (value != other.value)
				return false;
			return true;
		}
	}
	
	static class PathBuilder {
		Stack<TreeNode> nodes = new Stack<>();
		List<Path> maximalPaths = new ArrayList<>();

		public void construct() {
			Enumeration<TreeNode> elements = nodes.elements();
			Path p = new Path(elements.nextElement());
			while(elements.hasMoreElements()) {
				p.addNode(elements.nextElement());
			}
			maximalPaths.add(p);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PathBuilder other = (PathBuilder) obj;
			if (maximalPaths == null) {
				if (other.maximalPaths != null)
					return false;
			} else if (!maximalPaths.equals(other.maximalPaths))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			return true;
		}
	}
}
