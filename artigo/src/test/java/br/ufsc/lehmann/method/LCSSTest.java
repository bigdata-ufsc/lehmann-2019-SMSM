package br.ufsc.lehmann.method;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TimestampSemantic;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.SlackTemporalSemantic;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface LCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		AbstractProblem abstractProblem = (AbstractProblem) problem;
		StopSemantic stopSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.SPATIAL_LATLON;
		Semantic<Number, Number> xSemantic = null;
		Semantic<Number, Number> ySemantic = null;
		MutableInt geoThreshold = Thresholds.STOP_CENTROID_LATLON;
		Semantic<?, Number> timeSemantic = Semantic.TEMPORAL;
		Number timeThreshold = Thresholds.TEMPORAL;
		if(problem instanceof NElementProblem) {
			return new LCSSClassifier(
					new LCSSSemanticParameter<Stop, Number>(NElementProblem.stop, 0.5),
					new LCSSSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 0.5),
					new LCSSSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null)
					);
//		} else if(problem instanceof NewYorkBusProblem) {
//			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
//		} else if(problem instanceof DublinBusProblem) {
//			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
		} else if(problem instanceof GeolifeProblem) {
			GeolifeProblem geolifeProblem = (GeolifeProblem) problem;
			if(geolifeProblem.isRawTrajectory()) {
				timeSemantic = TimestampSemantic.TIMESTAMP_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
				geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
				geoThreshold = new MutableInt(4);
				xSemantic = GeolifeUniversityDatabaseReader.SPATIAL_X;
				ySemantic = GeolifeUniversityDatabaseReader.SPATIAL_Y;
			} else {
				timeSemantic = SlackTemporalSemantic.SLACK_TEMPORAL;
				timeThreshold = Thresholds.PROPORTION_TEMPORAL;
				geoThreshold = Thresholds.STOP_CENTROID_EUCLIDEAN;
			}
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
//		} else if(problem instanceof PatelProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((PatelProblem) problem).stopSemantic();
//		} else if(problem instanceof VehicleProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((VehicleProblem) problem).stopSemantic();
		} else if(problem instanceof SanFranciscoCabProblem) {
			SanFranciscoCabProblem sanFranciscoCabProblem = (SanFranciscoCabProblem) problem;
			if(sanFranciscoCabProblem.isRawTrajectory()) {
				timeSemantic = TimestampSemantic.TIMESTAMP_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
				geoThreshold = new MutableInt(100);
				xSemantic = SanFranciscoCabDatabaseReader.SPATIAL_X;
				ySemantic = SanFranciscoCabDatabaseReader.SPATIAL_Y;
			} else {
				timeSemantic = SlackTemporalSemantic.SLACK_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
				geoThreshold = Thresholds.STOP_CENTROID_EUCLIDEAN;
			}
			stopSemantic = sanFranciscoCabProblem.stopSemantic();
//		} else if(problem instanceof SergipeTracksProblem) {
//			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
//		} else if(problem instanceof PrototypeProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
//		} else if(problem instanceof PisaProblem) {
//			stopSemantic = ((PisaProblem) problem).stopSemantic();
//		} else if(problem instanceof HermoupolisProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((HermoupolisProblem) problem).stopSemantic();
		}
		if(abstractProblem.isRawTrajectory()) {
			return new LCSSClassifier(//
//				new LCSSSemanticParameter<Stop, Number>(stopSemantic, Thresholds.calculateThreshold(stopSemantic)),//
//				new LCSSSemanticParameter(timeSemantic, timeThreshold),
					new LCSSSemanticParameter<TPoint, Number>(geoSemantic, geoThreshold)
					);
		}
		return new LCSSClassifier(//
				new LCSSSemanticParameter<Stop, Number>(stopSemantic, Thresholds.calculateThreshold(stopSemantic)),//
					new LCSSSemanticParameter<TPoint, Number>(geoSemantic, geoThreshold)
					);
	}
}
