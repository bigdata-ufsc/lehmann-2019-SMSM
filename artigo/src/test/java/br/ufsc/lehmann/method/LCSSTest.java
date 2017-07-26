package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface LCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LCSSClassifier(new LCSSSemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 500),//
					new LCSSSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, 10)//
					);
		} else if(problem instanceof DublinBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, 500),//
					new LCSSSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, 10)//
					);
		} else if(problem instanceof PatelProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(PatelDataReader.STOP_SEMANTIC, 500),//
					new LCSSSemanticParameter(PatelDataReader.MOVE_SEMANTIC, 10)//
					);
		}
		return null;
	}
}
