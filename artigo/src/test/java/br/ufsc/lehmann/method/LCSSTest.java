package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
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
					new LCSSSemanticParameter(NElementProblem.move, Thresholds.MOVE_ANGLE),
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC, 0.5),
					new LCSSSemanticParameter(NElementProblem.dataSemantic, null)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON),//
					new LCSSSemanticParameter(NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)//
					);
		} else if(problem instanceof DublinBusProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((DublinBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON),//
					new LCSSSemanticParameter(DublinBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)//
					);
		} else if(problem instanceof PatelProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(PatelDataReader.STOP_CENTROID_SEMANTIC, Thresholds.GEOGRAPHIC_LATLON),//
					new LCSSSemanticParameter(PatelDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN)//
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON),//
					new LCSSSemanticParameter(SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)//
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON),//
					new LCSSSemanticParameter(SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON)//
					);
		} else if(problem instanceof PrototypeProblem) {
			return new LCSSClassifier(//
					new LCSSSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null),//
					new LCSSSemanticParameter(PrototypeDataReader.MOVE_SEMANTIC, null),//
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN)//
					);
		}
		return null;
	}
}
