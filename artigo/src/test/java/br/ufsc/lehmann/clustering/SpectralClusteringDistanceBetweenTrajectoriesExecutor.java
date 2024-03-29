package br.ufsc.lehmann.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.IClusteringExecutor;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import smile.clustering.SpectralClustering;

public class SpectralClusteringDistanceBetweenTrajectoriesExecutor implements IClusteringExecutor {
	
	private int classesCount;

	public SpectralClusteringDistanceBetweenTrajectoriesExecutor(int classesCount) {
		this.classesCount = classesCount;
	}

	@Override
	public <E, T> ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance, Semantic<E, T> discriminator) {
		if(!(measureDistance instanceof TrajectorySimilarityCalculator)) {
			throw new IllegalArgumentException("To clustering trajectories, measureDistance must be a TrajectorySimilarityCalculator!");
		}
		List<SemanticTrajectory> training = new ArrayList<>(data);
		double[][] distances = new double[training.size()][training.size()];
		for (int i = 0; i < training.size(); i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
				distances[finalI][j] = 1 - ((TrajectorySimilarityCalculator<SemanticTrajectory>)measureDistance).getSimilarity(training.get(finalI), training.get(j));
				distances[j][finalI] = distances[finalI][j];
			});
		}
		SpectralClustering clustering = new SpectralClustering(distances, classesCount);
		int[] clusterLabel = clustering.getClusterLabel();
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(i, training.get(i));
		}
		return new ClusteringResult(data, clusteres.asMap().values(), clusterLabel, new Comparator<SemanticTrajectory>() {

			@Override
			public int compare(SemanticTrajectory o1, SemanticTrajectory o2) {
				return discriminator.similarity(discriminator.getData(o1, 0), discriminator.getData(o2, 0)) == 1.0 ? 0 : 1;
			}
		});
	}
}
