package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NYBikeProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface MSMTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], null, 1.0));
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 500, .5),
					new MSMSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, 10, .5)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, 500, .5),
					new MSMSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, 10, .5)
					);
		} else if(problem instanceof NYBikeProblem) {
			return new MSMClassifier(new MSMSemanticParameter(problem.semantics()[0], 50, 1/3),
					new MSMSemanticParameter(problem.semantics()[1], 0.5, 1/3),
					new MSMSemanticParameter(problem.semantics()[2], null, 1/3));
		} else if(problem instanceof PatelProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(PatelDataReader.STOP_SEMANTIC, 500, .5),
					new MSMSemanticParameter(PatelDataReader.MOVE_SEMANTIC, 10, .5)
					);
		}
		return null;
	}
}
