package br.ufsc.lehmann.survey;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.ITrainable;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix.Tuple;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;

public abstract class AbstractPairClassClusteringEvaluation {
	
	protected void executeDescriptor(String fileName) {
		ExecutionPOJO execution;
		try {
			execution = new Gson().fromJson(new FileReader(fileName), ExecutionPOJO.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> similarityCalculator = Measures.createMeasures(measure);
		BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
		IDataReader dataReader = Datasets.createDataset(dataset);
		List<SemanticTrajectory> data = dataReader.read();
		
		List<Tuple<Tuple<Object, Object>, List<SemanticTrajectory>>> pairedClasses = pairClasses(data, groundtruthSemantic);
		for (TrajectorySimilarityCalculator<SemanticTrajectory> calculator : similarityCalculator) {
			Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) calculator);
	
			Stopwatch w = Stopwatch.createStarted();

			int errorCount = 0;
			for (Tuple<Tuple<Object, Object>, List<SemanticTrajectory>> tuple : pairedClasses) {
				List<SemanticTrajectory> pairData = tuple.getLast();
				if(calculator instanceof ITrainable) {
					((ITrainable) calculator).train(pairData);
				}
				double[][] distances = new double[pairData.size()][pairData.size()];
				IMeasureDistance<SemanticTrajectory> measurer = (IMeasureDistance<SemanticTrajectory>) calculator;
				for (int i = 0; i < pairData.size(); i++) {
					distances[i][i] = 0;
					final int finalI = i;
					IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
						distances[finalI][j] = measurer.distance(pairData.get(finalI), pairData.get(j));
						distances[j][finalI] = distances[finalI][j];
					});
				}
				ClusteringResult result = validation.cluster(pairData.toArray(new SemanticTrajectory[pairData.size()]), distances, 2);
				List<List<SemanticTrajectory>> clusteres = result.getClusteres();
				for (List<SemanticTrajectory> cluster : clusteres) {
					Stream<Object> classesInCluster = cluster.stream().map(t -> groundtruthSemantic.getData(t, 0)).distinct();
					//se o cluster contiver mais de uma classe este cluster está errado
					if(classesInCluster.count() != 1) {
						errorCount++;
					}
				}
			}
			System.out.printf("Elapsed time %d miliseconds\n", w.elapsed(TimeUnit.MILLISECONDS));
			System.out.printf("Parameters: '%s'\n", calculator.parametrization());
			System.out.printf("Total clusters: '%s'\n", pairedClasses.size() * 2);
			System.out.printf("Correct clusters: '%s'\n", (pairedClasses.size() * 2) - errorCount);
			w = w.stop();
		}
	}

	private List<Tuple<Tuple<Object, Object>, List<SemanticTrajectory>>> pairClasses(List<SemanticTrajectory> data, BasicSemantic<Object> semantic) {
		List<Object> allDistinctClasses = data.stream().map(t -> semantic.getData(t, 0)).distinct().collect(Collectors.toList());
		List<Tuple<Tuple<Object, Object>, List<SemanticTrajectory>>> ret = new ArrayList<>(allDistinctClasses.size() * 2);

		for (int i = 0; i < allDistinctClasses.size(); i++) {
			for (int j = i + 1; j < allDistinctClasses.size(); j++) {
				Tuple<Object, Object> pair = new Tuple<>(allDistinctClasses.get(i), allDistinctClasses.get(j));
				int finalI = i;
				int finalJ = j;
				List<SemanticTrajectory> stream = data.stream().filter(t -> Arrays.asList(allDistinctClasses.get(finalI), allDistinctClasses.get(finalJ)).contains(semantic.getData(t, 0))).collect(Collectors.toList());
				ret.add(new Tuple<>(pair, stream));
			}
		}
		
		return ret;
	}

}