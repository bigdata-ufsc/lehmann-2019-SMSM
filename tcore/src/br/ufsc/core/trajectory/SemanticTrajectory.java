package br.ufsc.core.trajectory;

import java.util.ArrayList;
import java.util.List;

public class SemanticTrajectory {

	private List<SemanticElement> elements = new ArrayList<>();
	private Object trajectoryId;
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

	public SemanticTrajectory(Object trajectoryId, int semantics) {
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

	public Object getTrajectoryId() {
		return trajectoryId;
	}

	@Override
	public String toString() {
		return "SemanticTrajectory [elements=" + elements + ", trajectoryId=" + trajectoryId + ", semantics=" + semantics + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + semantics;
		result = prime * result + ((trajectoryId == null) ? 0 : trajectoryId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SemanticTrajectory other = (SemanticTrajectory) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.containsAll(other.elements) || !other.elements.containsAll(elements))
			return false;
		if (semantics != other.semantics)
			return false;
		if (trajectoryId == null) {
			if (other.trajectoryId != null)
				return false;
		} else if (!trajectoryId.equals(other.trajectoryId))
			return false;
		return true;
	}
}
