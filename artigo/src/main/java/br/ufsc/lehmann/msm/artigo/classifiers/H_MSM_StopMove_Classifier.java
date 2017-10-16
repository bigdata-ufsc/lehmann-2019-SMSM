package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.H_MSM_StopMove;

public class H_MSM_StopMove_Classifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private H_MSM_StopMove msm;
	
	public H_MSM_StopMove_Classifier(H_MSM_StopMove.H_MSM_MoveSemanticParameters moveSemantic, H_MSM_StopMove.H_MSM_StopSemanticParameters stopSemantic) {
		msm = new H_MSM_StopMove(moveSemantic, stopSemantic);
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
		return "H_MSM_StopMove";
	}
}
