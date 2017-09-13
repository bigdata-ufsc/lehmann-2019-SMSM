package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
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

public interface LCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LCSSClassifier(
					new LCSSSemanticParameter(NElementProblem.stop, 0.5),
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC, 0.5),
					new LCSSSemanticParameter(NElementProblem.dataSemantic, null),
					new LCSSSemanticParameter(Semantic.TEMPORAL, 1)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), calculateThreshold(((NewYorkBusProblem) problem).stopSemantic())),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof DublinBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((DublinBusProblem) problem).stopSemantic(), calculateThreshold(((DublinBusProblem) problem).stopSemantic())),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof PatelProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((PatelProblem) problem).stopSemantic(), calculateThreshold(((PatelProblem) problem).stopSemantic())),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic())),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC)),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof PrototypeProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
					);
		} else if(problem instanceof PisaProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((PisaProblem) problem).stopSemantic(), calculateThreshold(((PisaProblem) problem).stopSemantic())),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new LCSSSemanticParameter(Semantic.TEMPORAL, Thresholds.TEMPORAL)//
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
