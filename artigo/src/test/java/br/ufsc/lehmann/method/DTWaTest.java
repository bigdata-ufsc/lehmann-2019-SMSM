package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWaClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface DTWaTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWaClassifier(problem, NElementProblem.stop, NElementProblem.move);
		} else if(problem instanceof PatelProblem) {
			return new DTWaClassifier(problem, PatelDataReader.STOP_SEMANTIC, PatelDataReader.MOVE_SEMANTIC);
		} else if(problem instanceof NewYorkBusProblem) {
			return new DTWaClassifier(problem, NewYorkBusDataReader.STOP_SEMANTIC, NewYorkBusDataReader.MOVE_SEMANTIC);
		} else if(problem instanceof DublinBusProblem) {
			return new DTWaClassifier(problem, DublinBusDataReader.STOP_SEMANTIC, DublinBusDataReader.MOVE_SEMANTIC);
		}
		return null;
	}
}
