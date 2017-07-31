package br.ufsc.lehmann.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IClusteringExecutor;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.CompleteLinkage;

public class HierarchicalClusteringDistanceBetweenTrajectoriesExecutor implements IClusteringExecutor {
	
	private int classesCount;

	public HierarchicalClusteringDistanceBetweenTrajectoriesExecutor(int classesCount) {
		this.classesCount = classesCount;
	}

	@Override
	public ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance) {
		List<SemanticTrajectory> training = new ArrayList<>(data);
		double[][] distances = new double[training.size()][training.size()];
		for (int i = 0; i < training.size(); i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
				distances[finalI][j] = measureDistance.distance(training.get(finalI), training.get(j));
				distances[j][finalI] = distances[finalI][j];
			});
		}
		HierarchicalClustering clustering = new HierarchicalClustering(new CompleteLinkage(distances));
		int[] clusterLabel = clustering.partition(classesCount);
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(clusterLabel[i], training.get(i));
		}
		return new ClusteringResult(clusteres.asMap().values(), clusterLabel);
	}

	@Override
	public ClusteringResult cluster(double[][] distances, SemanticTrajectory[] training, IMeasureDistance<SemanticTrajectory> measureDistance) {
		HierarchicalClustering clustering = new HierarchicalClustering(new CompleteLinkage(distances));
		int[] clusterLabel = clustering.partition(classesCount);
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(clusterLabel[i], training[i]);
		}
		return new ClusteringResult(clusteres.asMap().values(), clusterLabel);
	}

}
