package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.SWALEClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface SWALETest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new SWALEClassifier(new SWALE.SWALEParameters(0.0, -10, 10));
		} else if(problem instanceof NewYorkBusProblem) {
			return new SWALEClassifier(new SWALE.SWALEParameters(50, -10, 10));
		}
		if(problem instanceof PatelProblem) {
			return new SWALEClassifier(new SWALE.SWALEParameters(2, -10, 10));
		}
		return null;
	}
}
