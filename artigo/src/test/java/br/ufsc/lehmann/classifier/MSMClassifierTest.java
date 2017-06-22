package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class MSMClassifierTest extends AbstractClassifierTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], null, 1.0));
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], 50, 0.5),
					new MSMSemanticParameter(problem.semantics()[1], 100, 0.5));
		}
		return null;
	}

}
