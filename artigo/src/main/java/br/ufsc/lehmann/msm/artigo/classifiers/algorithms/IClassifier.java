package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import br.ufsc.core.trajectory.SemanticTrajectory;

public interface IClassifier<Label> {
	
	Label classify(SemanticTrajectory traj);

}
