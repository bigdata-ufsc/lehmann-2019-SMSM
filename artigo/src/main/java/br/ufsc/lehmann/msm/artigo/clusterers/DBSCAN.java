package br.ufsc.lehmann.msm.artigo.clusterers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.ml.clustering.Cluster;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class DBSCAN {

	public static void main(String[] args) throws Exception {
//		PatelProblem problem = new PatelProblem("animal");
		NewYorkBusProblem problem = new NewYorkBusProblem();
		List<SemanticTrajectory> data = new ArrayList<>(problem.data());
		IntStream.iterate(20, i -> i + 5).limit(4).parallel().forEach((threshold) -> {
//			EDR measurer = new EDR(new EDRSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, threshold));
			LCSSClassifier measurer = new LCSSClassifier(
					new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, threshold),//
					new LCSSSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, threshold * 2)//
					);
			IntStream.iterate(150, i -> i + 50).limit(4).parallel().forEach((meters) -> {
				kmeans(data, threshold, measurer, meters / 1000.0);
			});
		});
	}

	private static void kmeans(List<SemanticTrajectory> data, int threshold, IMeasureDistance<SemanticTrajectory> measurer, double epsilon) {
		SemanticTrajectoryDBSCAN kMeans = new SemanticTrajectoryDBSCAN(epsilon, 3, measurer);
		List<Cluster<ClusterableTrajectory>> cluster = kMeans.cluster(data.stream().map((t) -> new ClusterableTrajectory(t)).collect(Collectors.toList()));
		
		Multimap<Integer, String> tests = MultimapBuilder.hashKeys().arrayListValues().build();
		for (Cluster<ClusterableTrajectory> centroid : cluster) {
			List<ClusterableTrajectory> points = centroid.getPoints();
			for (ClusterableTrajectory t : points) {
				tests.put(centroid.hashCode(), NewYorkBusDataReader.ROUTE.getData(t.getTrajectory(), 0));
			}
		}
		System.out.printf("Threshold = %d, epsilon = %.2f\n", threshold, epsilon);
		Integer totalClustered = 0;
		for (Integer classId : tests.keySet()) {
			Collection<String> trajs = tests.get(classId);
			Map<String, Integer> r = new HashMap<>();
			for (String string : trajs) {
				r.put(string, r.getOrDefault(string, 0) + 1);
			}
			String t = "";
			for (String string : r.keySet()) {
				Integer clusteredByClass = r.get(string);
				t += string + "=" + clusteredByClass + " ";
				totalClustered += clusteredByClass;
			}
			System.out.println("Classe calculada " + classId + "/Classes reais: " + t);
		}
		System.out.println("Total agrupado: " + totalClustered);
	}
}
