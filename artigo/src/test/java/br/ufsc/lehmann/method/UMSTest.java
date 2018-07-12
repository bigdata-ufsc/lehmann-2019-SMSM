package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface UMSTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof GeolifeProblem) {
			return new UMS();
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new UMS();
		} else if(problem instanceof InvolvesProblem) {
			return new UMS();
		}
		return null;}
}
