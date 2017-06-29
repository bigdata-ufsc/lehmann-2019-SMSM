package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NYBikeProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class MSMClassifierTest extends AbstractClassifierTest {

	public MSMClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], null, 1.0));
		} else if(problem instanceof NewYorkBusProblem || problem instanceof DublinBusProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], 50, 0.5),
					new MSMSemanticParameter(problem.semantics()[1], 100, 0.5));
		}
		if(problem instanceof NYBikeProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], 50, 1/3),
					new MSMSemanticParameter(problem.semantics()[1], 0.5, 1/3),
					new MSMSemanticParameter(problem.semantics()[2], null, 1/3));
		}
		if(problem instanceof PatelProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], 500, 0.5),
					new MSMSemanticParameter(problem.semantics()[1], 100, 0.5));
		}
		return null;
	}

}
