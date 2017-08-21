package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.ComparableMoveSemantic;
import br.ufsc.lehmann.msm.artigo.ComparableStopSemantic;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSTPClassifier;
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

public interface MSTPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSTPClassifier(//
					NElementProblem.dataSemantic//
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSTPClassifier(//
					new ComparableStopSemantic(((NewYorkBusProblem) problem).stopSemantic()),//
					new ComparableMoveSemantic(NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC),//
					Semantic.GEOGRAPHIC_LATLON//
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSTPClassifier(//
					new ComparableStopSemantic(((DublinBusProblem) problem).stopSemantic()),//
					new ComparableMoveSemantic(DublinBusDataReader.MOVE_ANGLE_SEMANTIC),//
					Semantic.GEOGRAPHIC_LATLON//
					);
		} else if(problem instanceof PatelProblem) {
			return new MSTPClassifier(//
					new ComparableStopSemantic(PatelDataReader.STOP_CENTROID_SEMANTIC),//
					new ComparableMoveSemantic(PatelDataReader.MOVE_ANGLE_SEMANTIC),//
					Semantic.GEOGRAPHIC//
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSTPClassifier(//
					new ComparableStopSemantic(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC),//
					new ComparableMoveSemantic(SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC),//
					Semantic.GEOGRAPHIC_LATLON//
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSTPClassifier(//
					new ComparableStopSemantic(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC),//
					new ComparableMoveSemantic(SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC),//
					Semantic.GEOGRAPHIC_LATLON//
					);
		}
		return null;
	}
}
