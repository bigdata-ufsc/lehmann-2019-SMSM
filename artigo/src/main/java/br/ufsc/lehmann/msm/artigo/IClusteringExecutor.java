package br.ufsc.lehmann.msm.artigo;

import java.util.List;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;

public interface IClusteringExecutor {

	<E, T> ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance, Semantic<E, T> discriminator);

}
