package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface EDRTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 50),//
					new EDRSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, 10));
		} else if(problem instanceof DublinBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, 500),//
					new EDRSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, 10));
		}
		if(problem instanceof PatelProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(PatelDataReader.STOP_SEMANTIC, 500),//
					new EDRSemanticParameter(PatelDataReader.MOVE_SEMANTIC, 10));
		}
		return null;
	}
}
