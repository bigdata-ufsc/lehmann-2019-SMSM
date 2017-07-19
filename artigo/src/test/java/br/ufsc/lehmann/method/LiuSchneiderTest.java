package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface LiuSchneiderTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, problem.semantics()[1], null));
		}
		if(problem instanceof PatelProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, problem.semantics()[2], null));
		}
		return null;
	}
}
