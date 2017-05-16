package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class BasicSemantic<V> extends Semantic<V, Number> {

	public BasicSemantic(int index) {
		super(index);
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		Object aData = a.getDimensionData(index, i);
		Object bData = b.getDimensionData(index, j);
		return aData.equals(bData) ? 1 : 0;
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return distance(a, i, b, j).longValue() >= (threshold == null ? Long.MAX_VALUE : threshold.longValue());
	}

}
