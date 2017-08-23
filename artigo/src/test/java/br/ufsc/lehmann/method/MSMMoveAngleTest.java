package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.MSM_Move.MSMMoveSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMMoveClassifier;
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

public interface MSMMoveAngleTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMMoveClassifier(//
					new MSMMoveSemanticParameter(NElementProblem.stop, .5, NElementProblem.move, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.5, 0.5),//
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 0.5)
					);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, NewYorkBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1)
					);
		} else if(problem instanceof DublinBusProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(((DublinBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, DublinBusDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1)
					);
		} else if(problem instanceof PatelProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(PatelDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_EUCLIDEAN, PatelDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1)
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON, SanFranciscoCabDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1)
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON, SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC, Thresholds.MOVE_ANGLE, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON, 1)
					);
		} else if(problem instanceof PrototypeProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null, PrototypeDataReader.MOVE_SEMANTIC, null, .5)
					, new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1)
					);
		}
		return null;
	}
}
