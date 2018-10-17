package br.ufsc.lehmann.msm.artigo.problems;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TemporalSemantic;

public class BirthYearSemantic extends TemporalSemantic<String, Number>{

	public BirthYearSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(getData(a, i), getData(b, j)) == 0.0;
	}

	@Override
	public boolean match(String d1, String d2, Number threshlod) {
		return distance(d1, d2) == 0.0;
	}
	
	@Override
	public double distance(String d1, String d2) {
		return d1.equals(d2) ? 0.0 : 1.0;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance(getData(a, i), getData(b, j));
	}

}
