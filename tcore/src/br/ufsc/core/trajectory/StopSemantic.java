package br.ufsc.core.trajectory;

import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.Stop;

public class StopSemantic extends Semantic<Stop, Number> {

	private AttributeDescriptor<Stop> desc;

	public StopSemantic(int index, AttributeDescriptor<Stop> desc) {
		super(index);
		this.desc = desc;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance(getData(a, i), getData(b, j));
	}

	@Override
	public double distance(Stop d1, Stop d2) {
		if (d1 == d2) {
			return 0;
		}
		if (d1 == null || d2 == null) {
			return Double.MAX_VALUE;
		}
		return desc.distance(d1, d2);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return match(getData(a, i), getData(b, j), threshold);
	}

	public boolean match(Stop d1, Stop d2, Number threshold) {
		double distance = distance(d1, d2);
		if (threshold == null) {
			return distance == 0;
		}
		return distance <= desc.convertThreshold(threshold.doubleValue());
	}
	
	public String name() {
		return desc.attributeName();
	}

}
