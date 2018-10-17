package br.ufsc.core.trajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

public class SemanticTrajectory implements Comparable<SemanticTrajectory> {

	private List<SemanticElement> elements = new ArrayList<>();
	private Object trajectoryId;
	private int semantics;
	
	private Map<Semantic, StatisticalSummary> local = new HashMap<>();
	private Map<Semantic, StatisticalSummary> global = new HashMap<>();

	public SemanticTrajectory(Trajectory q) {
		this.trajectoryId = q.getTid();
		List<TPoint> points = q.getPoints();
		for (TPoint p : points) {
			SemanticElement se = new SemanticElement(1);
			se.addData(Semantic.SPATIAL, p);
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

	@Override
	public int compareTo(SemanticTrajectory o) {
		return String.valueOf(getTrajectoryId()).compareTo(String.valueOf(o.getTrajectoryId()));
	}

	public StatisticalSummary getLocalStats(Semantic<Object, Number> semantic) {
		return local.get(local.keySet().stream().filter(s -> s.index == semantic.index).findFirst().get());
	}

	public StatisticalSummary getGlobalStats(Semantic<Object, Number> semantic) {
		return global.get(global.keySet().stream().filter(s -> s.index == semantic.index).findFirst().get());
	}

	public void setLocalStats(Semantic semantic, StatisticalSummary trajStats) {
		local.put(semantic, trajStats);
	}

	public void setGlobalStats(Semantic semantic, StatisticalSummary summary) {
		global.put(semantic, summary);
		
	}
}
