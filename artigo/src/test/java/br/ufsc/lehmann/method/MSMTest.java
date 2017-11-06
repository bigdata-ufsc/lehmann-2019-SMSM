package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
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
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(NElementProblem.stop, 0.5, 1.0/4.0),
//					new MSMSemanticParameter(NElementProblem.move, Thresholds.MOVE_ANGLE, 0.25),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 1.0/4.0),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1.0/4.0),//
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/4.0)
//					new MSMSemanticParameter(NElementProblem.stopmove, 0.5, 1.0/3.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 1.0/3.0),//
//					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1.0/3.0)//
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((NewYorkBusProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(NewYorkBusDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/2.0)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((DublinBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((DublinBusProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(DublinBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(DublinBusDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/2.0)
					);
		} else if(problem instanceof PatelProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((PatelProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PatelProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(PatelDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(PatelDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/2.0)
					);
		} else if(problem instanceof VehicleProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((VehicleProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((VehicleProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(PatelDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(PatelDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/2.0)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(SanFranciscoCabDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/2.0)
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1.0/3.0),
//					new MSMSemanticParameter(SergipeTracksDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/2.0)
					);
		} else if(problem instanceof PrototypeProblem) {
			return new MSMClassifier(//
//					new MSMSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null, 1.0/3.0),
//					new MSMSemanticParameter(PrototypeDataReader.MOVE_SEMANTIC, null, 1.0/3.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0)
					new MSMSemanticParameter<Stop, Number>(PrototypeDataReader.STOP_SEMANTIC, null, 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
					);
		} else if(problem instanceof PisaProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter<Stop, Number>(((PisaProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PisaProblem) problem).stopSemantic()), 1.0/3.0),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0)
//					new MSMSemanticParameter(PisaDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1.0/2.0),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1.0/2.0)
					);
		}
		return null;
	}
}
