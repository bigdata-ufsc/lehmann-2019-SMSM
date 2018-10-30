package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;

public class LCSSClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private LCSS lcss;
	
	public LCSSClassifier(LCSSSemanticParameter... params) {
		lcss = new LCSS(params);
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return lcss.distance(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return lcss.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "LCSS";
	}

	@Override
	public String parametrization() {
		return this.lcss.parametrization();
	}
}
