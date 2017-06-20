package br.ufsc.lehmann.msm.artigo.clusterers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableLong;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity.CompleteLinkDissimilarity;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.AdjustedRandIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.DunnIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.MaxDistance;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import smile.clustering.SpectralClustering;

public class SpectralCluster {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		NewYorkBusProblem problem = new NewYorkBusProblem();
		List<SemanticTrajectory> training = new ArrayList<>(problem.trainingData());
		training.addAll(problem.testingData());
		double[][] distances = new double[training.size()][training.size()];
		LCSSClassifier lcssClassifier = new LCSSClassifier(new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, 50),
				new LCSSSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100));
		LCSS lcss = new LCSS(new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, 50),
				new LCSSSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100));
		for (int i = 0; i < training.size(); i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(training.size()).parallel().forEach((j) -> {
				if(finalI < j) {
					distances[finalI][j] = lcss.distance(training.get(finalI), training.get(j));
					distances[j][finalI] = distances[finalI][j];
				}
			});
		}
		SpectralClustering clustering = new SpectralClustering(distances, 2);
		int[] clusterLabel = clustering.getClusterLabel();
		Map<String, MutableLong> counters = new HashMap<>();
		for (int i = 0; i < clusterLabel.length; i++) {
			String data = NewYorkBusDataReader.ROUTE.getData(training.get(i), 0);
			MutableLong counter = counters.get(data + "_" + clusterLabel[i]);
			if(counter == null) {
				counter = new MutableLong(0L);
				counters.put(data + "_" + clusterLabel[i], counter);
			}
			counter.increment();
		}
		for (Map.Entry<String, MutableLong> entry : counters.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue().getValue());
		}
		AdjustedRandIndex<String> randIndex = new AdjustedRandIndex<String>();
		double value = randIndex.evaluate(clusterLabel, new Trajectories<>(training, problem.discriminator()));
		System.out.println("Adjusted Rand-index: " + value);
		DunnIndex<String> dunnIndex = new DunnIndex<>(new MaxDistance(lcssClassifier), new CompleteLinkDissimilarity(lcssClassifier));
		value = dunnIndex.evaluate(clusterLabel, new Trajectories<>(training, problem.discriminator()));
		System.out.println("Dunn index: " + value);
	}
}
