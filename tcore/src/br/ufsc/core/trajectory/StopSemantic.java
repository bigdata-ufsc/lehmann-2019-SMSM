package br.ufsc.core.trajectory;

import br.ufsc.core.trajectory.semantic.Stop;

public class StopSemantic extends Semantic<Stop, Number> {

	public StopSemantic(int index) {
		super(index);
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return false;
	}

}
