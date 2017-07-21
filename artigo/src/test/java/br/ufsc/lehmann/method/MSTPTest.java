package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.ComparableStopSemantic;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSTPClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface MSTPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSTPClassifier(problem.semantics()[0]);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSTPClassifier(new ComparableStopSemantic(NewYorkBusDataReader.STOP_SEMANTIC));
		} else if(problem instanceof DublinBusProblem) {
			return new MSTPClassifier(new ComparableStopSemantic(DublinBusDataReader.STOP_SEMANTIC));
		}
		if(problem instanceof PatelProblem) {
			return new MSTPClassifier(new ComparableStopSemantic(PatelDataReader.STOP_SEMANTIC));
		}
		return null;
	}
}
