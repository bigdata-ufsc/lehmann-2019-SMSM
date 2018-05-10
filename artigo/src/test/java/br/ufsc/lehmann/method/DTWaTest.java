package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TimestampSemantic;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.SlackTemporalSemantic;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWaClassifier;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface DTWaTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new DTWaClassifier(problem, NElementProblem.dataSemantic, Semantic.SPATIAL, Semantic.TEMPORAL);
//		} else if(problem instanceof PatelProblem) {
//			return new DTWaClassifier(problem, ((PatelProblem) problem).stopSemantic(), Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
//		} else if(problem instanceof VehicleProblem) {
//			return new DTWaClassifier(problem, ((VehicleProblem) problem).stopSemantic(), Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
//		} else if(problem instanceof NewYorkBusProblem) {
//			return new DTWaClassifier(problem, ((NewYorkBusProblem) problem).stopSemantic(), Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
//		} else if(problem instanceof DublinBusProblem) {
//			return new DTWaClassifier(problem, ((DublinBusProblem) problem).stopSemantic(), Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
		} else if(problem instanceof GeolifeProblem) {
			GeolifeProblem geolifeProblem = (GeolifeProblem) problem;
			if(geolifeProblem.isRawTrajectory()) {
				return new DTWaClassifier(problem, Semantic.SPATIAL, geolifeProblem.stopSemantic());
			}
			return new DTWaClassifier(problem, geolifeProblem.stopSemantic(), Semantic.SPATIAL);
		} else if(problem instanceof SanFranciscoCabProblem) {
			SanFranciscoCabProblem sanFranciscoCabProblem = (SanFranciscoCabProblem) problem;
			if(sanFranciscoCabProblem.isRawTrajectory()) {
				return new DTWaClassifier(problem, Semantic.SPATIAL_LATLON, TimestampSemantic.TIMESTAMP_TEMPORAL);
			}
			return new DTWaClassifier(problem, sanFranciscoCabProblem.stopSemantic(), Semantic.SPATIAL_LATLON, SlackTemporalSemantic.SLACK_TEMPORAL);
//		} else if(problem instanceof SergipeTracksProblem) {
//			return new DTWaClassifier(problem, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
//		} else if(problem instanceof PrototypeProblem) {
//			return new DTWaClassifier(problem, PrototypeDataReader.STOP_SEMANTIC, Semantic.SPATIAL_EUCLIDEAN, Semantic.TEMPORAL);
//		} else if(problem instanceof PisaProblem) {
//			return new DTWaClassifier(problem, ((PisaProblem) problem).stopSemantic(), Semantic.SPATIAL_LATLON, Semantic.TEMPORAL);
//		} else if(problem instanceof HermoupolisProblem) {
//			return new DTWaClassifier(problem, ((HermoupolisProblem) problem).stopSemantic(), Semantic.SPATIAL_EUCLIDEAN, Semantic.TEMPORAL);
		}
		return null;
	}
}
