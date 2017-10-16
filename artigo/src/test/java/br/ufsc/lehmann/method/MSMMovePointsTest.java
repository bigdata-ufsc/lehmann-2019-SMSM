package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.SMSM.SMSMSemanticParameter;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMMoveClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface MSMMovePointsTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMMoveClassifier(//
					new SMSMSemanticParameter(NElementProblem.stop, .5, NElementProblem.move_points, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 1.0/3.0),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, NewYorkBusDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((DublinBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, DublinBusDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof PatelProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((PatelProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_EUCLIDEAN, PatelDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof VehicleProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((VehicleProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_EUCLIDEAN, PatelDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, SanFranciscoCabDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, SergipeTracksDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof PrototypeProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null, PrototypeDataReader.MOVE_SEMANTIC, null, .5),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		} else if(problem instanceof PisaProblem) {
			return new MSMMoveClassifier(new SMSMSemanticParameter(((PisaProblem) problem).stopSemantic(), null, PisaDataReader.MOVE_POINTS_SEMANTIC, Thresholds.MOVE_INNER_POINTS_PERC, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, .5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, .5)
					);
		}
		return null;
	}
}
