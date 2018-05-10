package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.MoveSemantic;

public class ComparableMoveSemantic extends Semantic<Move, Number> {
	
	private MoveSemantic inner;

	public ComparableMoveSemantic(MoveSemantic inner) {
		super(-1);
		this.inner = inner;
	}
	
	@Override
	public ComparableMove getData(SemanticTrajectory p, int i) {
		Move data = inner.getData(p, i);
		if(data == null) {
			return null;
		}
		return new ComparableMove(data, new MoveComparator() {
			
			@Override
			public int compare(ComparableMove o1, ComparableMove o2) {
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
	public double distance(Move d1, Move d2) {
		return inner.distance(d1, d2);
	}

	@Override
	public boolean match(Move d1, Move d2, Number threshlod) {
		return inner.match(d1, d2, threshlod);
	}
}
