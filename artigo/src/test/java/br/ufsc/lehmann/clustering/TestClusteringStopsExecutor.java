package br.ufsc.lehmann.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.IClusteringExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import smile.clustering.SpectralClustering;

public class TestClusteringStopsExecutor implements IClusteringExecutor {
	
	private int classesCount;
	private StopSemantic semantic;
	private List<Stop> allStops;
	private Number threshold;

	public TestClusteringStopsExecutor(StopSemantic semantic, List<Stop> allStops, Number threshold, int classesCount) {
		this.semantic = semantic;
		this.allStops = allStops;
		this.threshold = threshold;
		this.classesCount = classesCount;
	}

	@Override
	public ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance) {
		List<SemanticTrajectory> training = new ArrayList<>(data);
		double[][] distances = new double[training.size()][allStops.size()];
		for (int i = 0; i < training.size(); i++) {
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(allStops.size()).parallel().forEach((j) -> {
				Stop stop = allStops.get(j);
				SemanticTrajectory traj = training.get(finalI);
				for (int k = 0; k < traj.length(); k++) {
					if(semantic.match(stop, semantic.getData(traj, k), threshold)) {
						distances[finalI][j] = 1;
						break;
					}
				}
			});
		}
		SpectralClustering clustering = new SpectralClustering(distances, classesCount);
		int[] clusterLabel = clustering.getClusterLabel();
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(i, training.get(i));
		}
		return new ClusteringResult(clusteres.asMap().values(), clusterLabel);
	}

}
