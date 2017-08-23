package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.HCSS.HCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface HCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC, .5), new WeightSemantic(-1, NElementProblem.stop));
		} else if(problem instanceof NewYorkBusProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON), new WeightSemantic(-1, ((NewYorkBusProblem) problem).stopSemantic()));
		} else if(problem instanceof DublinBusProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON), new WeightSemantic(-1, ((DublinBusProblem) problem).stopSemantic()));
		} else if(problem instanceof PatelProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN), new WeightSemantic(-1, PatelDataReader.STOP_CENTROID_SEMANTIC));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON), new WeightSemantic(-1, ((SanFranciscoCabProblem) problem).stopSemantic()));
		} else if(problem instanceof SergipeTracksProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, Thresholds.GEOGRAPHIC_LATLON), new WeightSemantic(-1, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC));
		} else if(problem instanceof PrototypeProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_EUCLIDEAN, Thresholds.GEOGRAPHIC_EUCLIDEAN), new WeightSemantic(-1, PrototypeDataReader.STOP_SEMANTIC));
		}
		return null;
	}
	
	public static class WeightSemantic extends BasicSemantic<Integer> {

		private Semantic semantic;

		public WeightSemantic(int index, Semantic semantic) {
			super(index);
			this.semantic = semantic;
		}
		
		@Override
		public Integer getData(SemanticTrajectory p, int i) {
			Object dimensionData = semantic.getData(p, i);
			return dimensionData != null ? 1 : 0;
		}
	}
}
