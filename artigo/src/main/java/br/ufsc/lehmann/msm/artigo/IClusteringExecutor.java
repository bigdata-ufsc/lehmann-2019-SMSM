package br.ufsc.lehmann.msm.artigo;

import java.util.List;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;

public interface IClusteringExecutor {

	ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance);

	ClusteringResult cluster(double[][] distances, SemanticTrajectory[] training, IMeasureDistance<SemanticTrajectory> measureDistance);
}
