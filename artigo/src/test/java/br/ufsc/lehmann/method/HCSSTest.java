package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.HCSS.HCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface HCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC, new TPoint(0,0)), new WeightSemantic(-1, NElementProblem.stop));
		} else if(problem instanceof NewYorkBusProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, new TPoint(0,0)), new WeightSemantic(-1, NewYorkBusDataReader.STOP_SEMANTIC));
		} else if(problem instanceof DublinBusProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, new TPoint(0,0)), new WeightSemantic(-1, DublinBusDataReader.STOP_SEMANTIC));
		} else if(problem instanceof PatelProblem) {
			return new HCSS(new HCSSSemanticParameter(Semantic.GEOGRAPHIC, new TPoint(0,0)), new WeightSemantic(-1, PatelDataReader.STOP_SEMANTIC));
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
