package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;

public interface DTWTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWClassifier(NElementProblem.dataSemantic);
		} else if(problem instanceof PatelProblem) {
			return new DTWClassifier(PatelDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof NewYorkBusProblem) {
			return new DTWClassifier(((NewYorkBusProblem) problem).stopSemantic());
		} else if(problem instanceof DublinBusProblem) {
			return new DTWClassifier(((DublinBusProblem) problem).stopSemantic());
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new DTWClassifier(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof SergipeTracksProblem) {
			return new DTWClassifier(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC);
		}
		return null;
	}
}
