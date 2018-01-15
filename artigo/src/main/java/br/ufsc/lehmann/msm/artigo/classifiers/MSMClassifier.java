package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;

public class MSMClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	MSM msm;
	
	public MSMClassifier(MSMSemanticParameter... params) {
		msm = new MSM(params);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return msm.distance(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return msm.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "MSM";
	}
}
