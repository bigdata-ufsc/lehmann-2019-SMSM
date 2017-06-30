package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.MSM_Move;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

public class MSMMoveClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private MSM_Move msm;
	
	public MSMMoveClassifier(MSMSemanticParameter<Move, Number> moveSemantic, MSMSemanticParameter<?, ?>... params) {
		msm = new MSM_Move(moveSemantic, params);
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
		return "MSM_Move";
	}
}
