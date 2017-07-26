package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface DTWTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWClassifier(problem.semantics()[0]);
		} else if(problem instanceof PatelProblem) {
			return new DTWClassifier(PatelDataReader.STOP_SEMANTIC);
		} else if(problem instanceof NewYorkBusProblem) {
			return new DTWClassifier(NewYorkBusDataReader.STOP_SEMANTIC);
		} else if(problem instanceof DublinBusProblem) {
			return new DTWClassifier(DublinBusDataReader.STOP_SEMANTIC);
		}
		return null;
	}
}
