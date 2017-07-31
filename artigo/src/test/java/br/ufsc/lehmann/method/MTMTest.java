package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MTMClassifier;

public interface MTMTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new MTMClassifier(problem);
	}
}
