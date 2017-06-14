package br.ufsc.lehmann.msm.artigo.problems;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class BasicSemantic<V> extends Semantic<V, Number> {

	public BasicSemantic(int index) {
		super(index);
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		V aData = (V) getData(a, i);
		V bData = (V) getData(b, j);
		return distance(aData, bData);
	}
	
	@Override
	public double distance(V d1, V d2) {
		return d1.equals(d2) ? 0 : 1;
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return distance(a, i, b, j).longValue() <= (threshold == null ? 0 : threshold.longValue());
	}

}
