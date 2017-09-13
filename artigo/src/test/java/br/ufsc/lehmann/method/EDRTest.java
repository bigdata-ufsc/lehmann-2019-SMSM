package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;
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

public interface EDRTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(NElementProblem.stop, 0.5),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(NElementProblem.dataSemantic, null),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof NewYorkBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), calculateThreshold(((NewYorkBusProblem) problem).stopSemantic())),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof DublinBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(((DublinBusProblem) problem).stopSemantic(), calculateThreshold(((DublinBusProblem) problem).stopSemantic())),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof PatelProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(((PatelProblem) problem).stopSemantic(), calculateThreshold(((PatelProblem) problem).stopSemantic())),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic())),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof SergipeTracksProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC)),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof PrototypeProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
		} else if(problem instanceof PisaProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(((PisaProblem) problem).stopSemantic(), calculateThreshold(((PisaProblem) problem).stopSemantic())),//
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL));
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
