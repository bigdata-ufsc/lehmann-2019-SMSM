package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface LCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LCSSClassifier(new LCSSSemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(problem.semantics()[0], 50),//
					new LCSSSemanticParameter(problem.semantics()[1], null)//
					);
		}
		if(problem instanceof PatelProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(problem.semantics()[0], 500),//
					new LCSSSemanticParameter(problem.semantics()[1], 100)//
					);
		}
		return null;
	}
}
