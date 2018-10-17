package br.ufsc.lehmann.msm.artigo.problems;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.IDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class BasicSemantic<V> extends Semantic<V, Number> {
	
	private IDistanceFunction<V> distance = new EqualsDistanceFunction<>();

	public BasicSemantic(int index) {
		this(index, new EqualsDistanceFunction<>());
	}

	public BasicSemantic(int index, IDistanceFunction<V> distance) {
		super(index);
		this.distance = distance;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		V aData = (V) getData(a, i);
		V bData = (V) getData(b, j);
		return distance(aData, bData);
	}
	
	@Override
	public double distance(V d1, V d2) {
		return distance.distance(d1, d2);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return distance(a, i, b, j).doubleValue() <= (threshold == null ? 0 : threshold.doubleValue());
	}

	@Override
	public boolean match(V d1, V d2, Number threshold) {
		return distance(d1, d2) <= (threshold == null ? 0 : threshold.doubleValue());
	}

}
