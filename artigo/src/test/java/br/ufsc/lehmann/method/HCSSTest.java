package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.HCSS.HCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface HCSSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new HCSS(new HCSSSemanticParameter(problem.semantics()[0], null), new WeightSemantic(-1, 0));
		} else if(problem instanceof NewYorkBusProblem) {
			return new HCSS(new HCSSSemanticParameter(problem.semantics()[1], null), new WeightSemantic(-1, 10/*NewYorkBusDataReader.STOP_SEMANTIC*/));
		}
		if(problem instanceof PatelProblem) {
			return new HCSS(new HCSSSemanticParameter(problem.semantics()[2], null), new WeightSemantic(-1, 5/*PatelDataReader.STOP_SEMANTIC*/));
		}
		return null;
	}
	
	public static class WeightSemantic extends BasicSemantic<Integer> {

		private int stopIndex;

		public WeightSemantic(int index, int stopIndex) {
			super(index);
			this.stopIndex = stopIndex;
		}
		
		@Override
		public Integer getData(SemanticTrajectory p, int i) {
			Object dimensionData = p.getDimensionData(stopIndex, i);
			return dimensionData != null ? 1 : 0;
		}
	}
}
