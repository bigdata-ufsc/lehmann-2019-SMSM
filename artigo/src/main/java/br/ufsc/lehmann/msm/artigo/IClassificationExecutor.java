package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.SemanticTrajectory;

public interface IClassificationExecutor {

	void classify(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance);
}
