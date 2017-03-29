package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class BirthYearSemantic extends Semantic<String, Void>{

	public BirthYearSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Void threshlod) {
		return a.getDimensionData(index, i).equals(b.getDimensionData(index, j));
	}

}
