package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
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
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface MSMTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(NElementProblem.stop, 0.5, 1/4),
//					new MSMSemanticParameter(NElementProblem.move, Thresholds.MOVE_ANGLE, 0.25),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 1/4),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1/4),//
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/4)
//					new MSMSemanticParameter(NElementProblem.stopmove, 0.5, 1/3),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 1/3),//
//					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 1/3)//
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), calculateThreshold(((NewYorkBusProblem) problem).stopSemantic()), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
//					new MSMSemanticParameter(NewYorkBusDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/2)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(((DublinBusProblem) problem).stopSemantic(), calculateThreshold(((DublinBusProblem) problem).stopSemantic()), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(DublinBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
//					new MSMSemanticParameter(DublinBusDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/2)
					);
		} else if(problem instanceof PatelProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(((PatelProblem) problem).stopSemantic(), calculateThreshold(((PatelProblem) problem).stopSemantic()), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(PatelDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
//					new MSMSemanticParameter(PatelDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1/2)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic()), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
//					new MSMSemanticParameter(SanFranciscoCabDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/2)
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
//					new MSMSemanticParameter(SergipeTracksDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/2)
					);
		} else if(problem instanceof PrototypeProblem) {
			return new MSMClassifier(//
//					new MSMSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null, 1/3),
//					new MSMSemanticParameter(PrototypeDataReader.MOVE_SEMANTIC, null, 1/3),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1/3)
					new MSMSemanticParameter(PrototypeDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
					);
		} else if(problem instanceof PisaProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(((PisaProblem) problem).stopSemantic(), calculateThreshold(((PisaProblem) problem).stopSemantic()), 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL, 1/3)
//					new MSMSemanticParameter(PisaDataReader.STOP_MOVE_COMBINED, Thresholds.STOP_MOVE, 1/2),
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/2)
					);
		}
		return null;
	}
	
	public static double calculateThreshold(StopSemantic semantic) {
		if(semantic.name().equals(AttributeType.STOP_CENTROID.name())) {
			return Thresholds.STOP_CENTROID_LATLON;
		}
		if(semantic.name().equals(AttributeType.STOP_STREET_NAME.name())) {
			return Thresholds.STOP_STREET_NAME;
		}
		return Double.MAX_VALUE;
	}
}
