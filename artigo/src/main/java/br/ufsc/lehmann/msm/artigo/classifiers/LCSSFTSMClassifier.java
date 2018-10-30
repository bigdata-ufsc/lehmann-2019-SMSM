package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.method.lcss.FTSMQLCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;

public class LCSSFTSMClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private FTSMQLCSS lcss;
	
	public LCSSFTSMClassifier(LCSSSemanticParameter... params) {
		if(params.length > 1) {
			throw new IllegalArgumentException("LCSS-FTSM only accept spatial dimension");
		}
		lcss = new FTSMQLCSS(params[0]);
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1  - lcss.getSimilarity((t1), (t2));
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return lcss.getSimilarity((t1), (t2));
	}

	@Override
	public String name() {
		return "LCSS-FTSM";
	}

	@Override
	public String parametrization() {
		return this.lcss.parametrization();
	}
}
