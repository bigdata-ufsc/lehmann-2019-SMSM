package br.ufsc.core.trajectory;

import java.util.ArrayList;
import java.util.List;

public class SemanticTrajectory {

	private List<SemanticElement> elements = new ArrayList<>();
	private Integer trajectoryId;
	private int semantics;

	public SemanticTrajectory(Trajectory q) {
		this.trajectoryId = q.getTid();
		List<TPoint> points = q.getPoints();
		for (TPoint p : points) {
			SemanticElement se = new SemanticElement(1);
			se.addData(Semantic.GEOGRAPHIC, p);
			elements.add(se);
		}
		this.semantics = 1;
	}

	public SemanticTrajectory(Integer trajectoryId, int semantics) {
		this.trajectoryId = trajectoryId;
		this.semantics = semantics;
	}

	public int length() {
		return elements.size();
	}

	public Object getDimensionData(int index, int i) {
		return elements.get(i).getData(index);
	}

	public void setDimensionData(int i, int index, Object p) {
		elements.get(i).setData(index, p);
	}

	public <V> void addData(int i, Semantic<V, ?> semantic, V ds) {
		if(elements.size() <= i) {
			for (int j = elements.size(); j <= i; j++) {
				elements.add(new SemanticElement(semantics));
			}
		}
		elements.get(i).addData(semantic, ds);
	}

	public int semanticsCount() {
		return semantics;
	}
}
