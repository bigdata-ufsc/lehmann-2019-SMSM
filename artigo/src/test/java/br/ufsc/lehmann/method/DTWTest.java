package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWClassifier;

public interface DTWTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new DTWClassifier(problem.semantics()[0]);
	}
}
