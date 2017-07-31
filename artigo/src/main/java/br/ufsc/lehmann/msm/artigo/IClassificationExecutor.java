package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;

public interface IClassificationExecutor {

	void classifyProblem(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance);
}
