package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.method.msm.FTSMQMSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;

public class MSMFTSMClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	FTSMQMSM msm;
	
	public MSMFTSMClassifier(MSMSemanticParameter params) {
		msm = new FTSMQMSM(params);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1 - getSimilarity(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return msm.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "MSM-FTSM";
	}

	
	@Override
	public String parametrization() {
		return this.msm.parametrization();
	}
}
