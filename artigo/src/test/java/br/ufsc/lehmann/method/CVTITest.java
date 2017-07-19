package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface CVTITest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[1], 100));
		}
		if(problem instanceof PatelProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[2], null));
		}
		return null;
	}
}
