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
		SemanticElement se = elements.get(i);
		return se.getData(index);
	}

	public void setDimensionData(int i, int index, Object p) {
		SemanticElement se = elements.get(i);
		se.setData(index, p);
	}

	public <V> void addData(int i, Semantic<V, ?> semantic, V ds) {
		if(elements.size() <= i) {
			for (int j = elements.size(); j <= i; j++) {
				elements.add(new SemanticElement(semantics));
			}
		}
		SemanticElement se = elements.get(i);
		se.addData(semantic, ds);
	}

	public int semanticsCount() {
		return semantics;
	}

	public Integer getTrajectoryId() {
		return trajectoryId;
	}

	@Override
	public String toString() {
		return "SemanticTrajectory [elements=" + elements + ", trajectoryId=" + trajectoryId + ", semantics=" + semantics + "]";
	}
}
