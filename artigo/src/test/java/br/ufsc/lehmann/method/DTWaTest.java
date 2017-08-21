package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWaClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;

public interface DTWaTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWaClassifier(problem, NElementProblem.dataSemantic, Semantic.GEOGRAPHIC);
		} else if(problem instanceof PatelProblem) {
			return new DTWaClassifier(problem, PatelDataReader.STOP_CENTROID_SEMANTIC, PatelDataReader.MOVE_ANGLE_SEMANTIC, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof NewYorkBusProblem) {
			return new DTWaClassifier(problem, ((NewYorkBusProblem) problem).stopSemantic(), NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof DublinBusProblem) {
			return new DTWaClassifier(problem, ((DublinBusProblem) problem).stopSemantic(), DublinBusDataReader.MOVE_ANGLE_SEMANTIC, Semantic.GEOGRAPHIC);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new DTWaClassifier(problem, SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof SergipeTracksProblem) {
			return new DTWaClassifier(problem, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Semantic.GEOGRAPHIC_LATLON);
		}
		return null;
	}
}
