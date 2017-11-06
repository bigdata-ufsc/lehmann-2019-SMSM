package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;
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

public interface EDRTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(NElementProblem.stop, 0.5),
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((NewYorkBusProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((DublinBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((DublinBusProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof PatelProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((PatelProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PatelProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof VehicleProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((VehicleProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((VehicleProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC)),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof PrototypeProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(PrototypeDataReader.STOP_SEMANTIC, null),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		} else if(problem instanceof PisaProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter<Stop, Number>(((PisaProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PisaProblem) problem).stopSemantic())),//
					new EDRSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)
//					new EDRSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, Thresholds.TEMPORAL)
					);
		}
		return null;
	}
}
