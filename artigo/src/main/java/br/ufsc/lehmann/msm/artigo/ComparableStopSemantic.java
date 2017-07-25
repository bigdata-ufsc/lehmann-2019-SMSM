package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.Stop;

public class ComparableStopSemantic extends Semantic<Stop, Number> {
	
	private StopSemantic inner;

	public ComparableStopSemantic(StopSemantic inner) {
		super(-1);
		this.inner = inner;
	}
	
	@Override
	public ComparableStop getData(SemanticTrajectory p, int i) {
		Stop data = inner.getData(p, i);
		if(data == null) {
			return null;
		}
		return new ComparableStop(data, new StopComparator() {
			
			@Override
			public int compare(ComparableStop o1, ComparableStop o2) {
				return (int) inner.distance(o1, o2);
			}
		});
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return inner.distance(a, i, b, j);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return inner.match(a, i, b, j, threshlod);
	}

	@Override
	public double distance(Stop d1, Stop d2) {
		return inner.distance(d1, d2);
	}

	@Override
	public boolean match(Stop d1, Stop d2, Number threshlod) {
		return inner.match(d1, d2, threshlod);
	}
}
