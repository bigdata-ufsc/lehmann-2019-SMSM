package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface DTWTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWClassifier(NElementProblem.dataSemantic);
		} else if(problem instanceof PatelProblem) {
			return new DTWClassifier(((PatelProblem) problem).stopSemantic());
		} else if(problem instanceof NewYorkBusProblem) {
			return new DTWClassifier(((NewYorkBusProblem) problem).stopSemantic());
		} else if(problem instanceof DublinBusProblem) {
			return new DTWClassifier(((DublinBusProblem) problem).stopSemantic());
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new DTWClassifier(((SanFranciscoCabProblem) problem).stopSemantic());
		} else if(problem instanceof SergipeTracksProblem) {
			return new DTWClassifier(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof PrototypeProblem) {
			return new DTWClassifier(PrototypeDataReader.STOP_SEMANTIC);
		} else if(problem instanceof PisaProblem) {
			return new DTWClassifier(((PisaProblem) problem).stopSemantic());
		}
		return null;
	}
}
