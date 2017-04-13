package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class MTM extends TrajectorySimilarityCalculator<SemanticTrajectory> {


	public MTM() {
	}

	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return -1;
	}
}
