package br.ufsc.lehmann.method;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.TimestampSemantic;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.SlackTemporalSemantic;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface MSMTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.SPATIAL_LATLON;
		MutableInt geoThreshold = Thresholds.SPATIAL_LATLON;
		Semantic<?, Number> timeSemantic = Semantic.TEMPORAL;
		Number timeThreshold = Thresholds.TEMPORAL;
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(NElementProblem.stop, 0.5, 1.0/4.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 0.5, 1.0/4.0),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1.0/4.0),//
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/4.0)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
		} else if(problem instanceof DublinBusProblem) {
			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
		} else if(problem instanceof GeolifeProblem) {
			GeolifeProblem geolifeProblem = (GeolifeProblem) problem;
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
			if(geolifeProblem.isRawTrajectory()) {
				timeSemantic = TimestampSemantic.TIMESTAMP_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
			} else {
				timeSemantic = SlackTemporalSemantic.SLACK_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
			}
		} else if(problem instanceof PatelProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((PatelProblem) problem).stopSemantic();
		} else if(problem instanceof VehicleProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((VehicleProblem) problem).stopSemantic();
		} else if(problem instanceof SanFranciscoCabProblem) {
			SanFranciscoCabProblem sanFranciscoCabProblem = (SanFranciscoCabProblem) problem;
			if(sanFranciscoCabProblem.isRawTrajectory()) {
				timeSemantic = TimestampSemantic.TIMESTAMP_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
			} else {
				timeSemantic = SlackTemporalSemantic.SLACK_TEMPORAL;
				timeThreshold = Thresholds.SLACK_TEMPORAL;
			}
			stopSemantic = sanFranciscoCabProblem.stopSemantic();
		} else if(problem instanceof SergipeTracksProblem) {
			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
		} else if(problem instanceof PrototypeProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
		} else if(problem instanceof PisaProblem) {
			stopSemantic = ((PisaProblem) problem).stopSemantic();
		} else if(problem instanceof HermoupolisProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((HermoupolisProblem) problem).stopSemantic();
		}
		return new MSMClassifier(//
//					new MSMSemanticParameter<TemporalDuration, Number>(SlackTemporalSemantic.SLACK_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
//					new MSMSemanticParameter<TPoint, Number>(geoSemantic, geoThreshold.intValue(), 1.0/3.0),
					new MSMSemanticParameter<Stop, Number>(stopSemantic, Thresholds.calculateThreshold(stopSemantic), 1.0/2.0),
//				new MSMSemanticParameter(timeSemantic, timeThreshold, 1.0/2.0),
					new MSMSemanticParameter<TPoint, Number>(geoSemantic, geoThreshold, 1.0/2.0)
				);
	}
}
