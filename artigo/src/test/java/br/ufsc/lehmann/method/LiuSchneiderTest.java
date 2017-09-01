package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface LiuSchneiderTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, NElementProblem.stop, 0.5));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((NewYorkBusProblem) problem).stopSemantic(), calculateThreshold(((NewYorkBusProblem) problem).stopSemantic())));
		} else if(problem instanceof DublinBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((DublinBusProblem) problem).stopSemantic(), calculateThreshold(((DublinBusProblem) problem).stopSemantic())));
		} else if(problem instanceof PatelProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((PatelProblem) problem).stopSemantic(), calculateThreshold(((PatelProblem) problem).stopSemantic())));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((SanFranciscoCabProblem) problem).stopSemantic(), calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic())));
		} else if(problem instanceof SergipeTracksProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC)));
		} else if(problem instanceof PrototypeProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, PrototypeDataReader.STOP_SEMANTIC, null));
		} else if(problem instanceof PisaProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((PisaProblem) problem).stopSemantic(), calculateThreshold(((PisaProblem) problem).stopSemantic())));
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
