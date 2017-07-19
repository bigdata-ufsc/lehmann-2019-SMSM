package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface EDRTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(problem.semantics()[0], 50),//
					new EDRSemanticParameter(problem.semantics()[1], null));
		}
		if(problem instanceof PatelProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(problem.semantics()[0], 50),//
					new EDRSemanticParameter(problem.semantics()[1], 100),//
					new EDRSemanticParameter(problem.semantics()[2], null));
		}
		return null;
	}
}
