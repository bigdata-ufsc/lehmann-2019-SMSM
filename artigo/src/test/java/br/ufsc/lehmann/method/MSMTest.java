package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
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
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;

public interface MSMTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMClassifier(new MSMSemanticParameter(NElementProblem.stop, 0.5, 0.25),
					new MSMSemanticParameter(NElementProblem.move, Thresholds.MOVE_ANGLE, 0.25),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 0.25),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 0.25));
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, 1/3),
					new MSMSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 0.25)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, 1/3),
					new MSMSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3)
					);
		} else if(problem instanceof PatelProblem) {
			return new MSMClassifier(//
					new MSMSemanticParameter(PatelDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_EUCLIDEAN, 1/3),
					new MSMSemanticParameter(PatelDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1/3)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSMClassifier(new MSMSemanticParameter(SanFranciscoCabDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, 1/3),
					new MSMSemanticParameter(SanFranciscoCabDataReader.MOVE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3));
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSMClassifier(new MSMSemanticParameter(SergipeTracksDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, 1/3),
					new MSMSemanticParameter(SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, 1/3),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1/3));}
		return null;
	}
}
