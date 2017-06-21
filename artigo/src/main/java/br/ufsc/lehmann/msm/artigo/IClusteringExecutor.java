package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;

public interface IClusteringExecutor {

	ClusteringResult cluster(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance);
}
