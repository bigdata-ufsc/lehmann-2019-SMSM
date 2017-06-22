package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.LiuSchneider;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class LiuSchneiderClassifierTest extends AbstractClassifierTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, problem.semantics()[1], 100));
		}
		return null;
	}

}
