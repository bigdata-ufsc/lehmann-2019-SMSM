package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;

public abstract class AbstractClusteringEvaluation {
	
	private int[] clusterSizes;

	public AbstractClusteringEvaluation(int[] clusterSizes) {
		this.clusterSizes = clusterSizes;
	}

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
		
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		for (TrajectorySimilarityCalculator<SemanticTrajectory> calculator : similarityCalculator) {
			Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) calculator);
	
			Stopwatch w = Stopwatch.createStarted();
			if(calculator instanceof ITrainable) {
				((ITrainable) calculator).train(Arrays.asList(allData));
			}
			double[][] distances = new double[allData.length][allData.length];
			IMeasureDistance<SemanticTrajectory> measurer = (IMeasureDistance<SemanticTrajectory>) calculator;
			for (int i = 0; i < allData.length; i++) {
				distances[i][i] = 0;
				final int finalI = i;
				IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
					distances[finalI][j] = measurer.distance(allData[finalI], allData[j]);
					distances[j][finalI] = distances[finalI][j];
				});
			}
			w = w.stop();
			System.out.printf("Elapsed time %d miliseconds\n", w.elapsed(TimeUnit.MILLISECONDS));
			IntStream.of(this.clusterSizes).forEach(numberOfClusters -> {
				ClusteringResult result = validation.cluster(allData, distances, numberOfClusters);
				System.out.printf("Number of clusters: %d\n", numberOfClusters);
				System.out.printf("Parameters: '%s'\n", calculator.parametrization());
				System.out.printf("F-Measure: %.8f\n", result.fMeasure());
			});;
		}
	}

}