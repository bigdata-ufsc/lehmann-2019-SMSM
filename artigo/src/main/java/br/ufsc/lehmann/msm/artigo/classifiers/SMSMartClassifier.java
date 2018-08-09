package br.ufsc.lehmann.msm.artigo.classifiers;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.SMSM;
import br.ufsc.lehmann.SMSMart;

public class SMSMartClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private SMSMart smsm;
	
	public SMSMartClassifier(SMSM.SMSM_MoveSemanticParameters moveSemantic, SMSM.SMSM_StopSemanticParameters stopSemantic) {
		smsm = new SMSMart(moveSemantic, stopSemantic);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1 - getSimilarity(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return smsm.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "SMSMart";
	}
}
