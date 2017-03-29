package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class UserTypeSemantic extends Semantic<String, Void>{

	public UserTypeSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Void threshlod) {
		return a.getDimensionData(index, i).equals(b.getDimensionData(index, j));
	}

}
