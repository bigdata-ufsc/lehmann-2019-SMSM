package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public interface wDFTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new wDF(3);
	}
}
