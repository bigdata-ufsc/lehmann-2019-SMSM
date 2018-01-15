package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.SMSM;

public class SMSMClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private SMSM msm;
	
	public SMSMClassifier(SMSM.H_MSM_MoveSemanticParameters moveSemantic, SMSM.H_MSM_StopSemanticParameters stopSemantic) {
		msm = new SMSM(moveSemantic, stopSemantic);
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
		return "SMSM";
	}
}
