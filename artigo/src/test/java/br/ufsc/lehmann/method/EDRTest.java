package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
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
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface EDRTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(NElementProblem.stop, 0.5),
					new EDRSemanticParameter(NElementProblem.move, Thresholds.MOVE_ANGLE),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON),
					new EDRSemanticParameter(NElementProblem.dataSemantic, null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON),//
					new EDRSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON));
		} else if(problem instanceof DublinBusProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON),//
					new EDRSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON));
		} else if(problem instanceof PatelProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(PatelDataReader.STOP_SEMANTIC, Thresholds.GEOGRAPHIC_LATLON),//
					new EDRSemanticParameter(PatelDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new EDRClassifier(//
					new EDRSemanticParameter(SanFranciscoCabDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON),//
					new EDRSemanticParameter(SanFranciscoCabDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE),
					new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON));
		}
		return null;
	}
}
