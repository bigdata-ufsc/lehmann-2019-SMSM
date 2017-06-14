package br.ufsc.ftsm.base;

import java.io.IOException;

public abstract class TrajectorySimilarityCalculator<Traj> {
	public abstract double getSimilarity(Traj t1,Traj t2);

	public void init() throws IOException {
		
	}

	public void destroy() {
		
	}
}
