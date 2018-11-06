package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.ITrainable;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.IntraclassAnalysis.IntraclassResults;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.MAP;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;
import smile.math.Random;

public class IntraclassAnalysis {


	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		Stream<java.nio.file.Path> files = java.nio.file.Files.walk(Paths.get("./src/test/resources/geolife"));
		final Map<String, IntraclassResults> results = new HashMap<>();
		files.filter(path -> path.toFile().isFile() && path.toString().contains("zDTWa") && path.toFile().toString().endsWith(".test")).forEach(path -> {
			String fileName = path.toString();
			System.out.printf("Executing file %s\n", fileName);
			
			IntraclassResults executeDescriptor = executeDescriptor(fileName);
			results.put(fileName, executeDescriptor);
		});
		String[][] r = new String[results.size() + 1][];
		Map<Object, DescriptiveStatistics> stats = results.entrySet().stream().findFirst().get().getValue().stats;
		r[0] = stats.keySet().stream().map(String::valueOf).collect(Collectors.toList()).toArray(new String[stats.size()]);
		r[0] = ArrayUtils.addAll(new String[] {""}, r[0]);
		final MutableInt i = new MutableInt(1);
		results.entrySet().stream().forEach(entry -> {
			String[] array = entry.getValue().stats.values().stream().map(DescriptiveStatistics::getMean).map(String::valueOf).collect(Collectors.toList()).toArray(new String[entry.getValue().stats.size()]);
			r[i.getValue()] = ArrayUtils.addAll(new String[] {entry.getValue().measure}, array);
			i.increment();
		});
		System.out.println(Arrays
		        .stream(r)
		        .map(Arrays::toString) 
		        .collect(Collectors.joining(System.lineSeparator())));
		files.close();
	}

	private static IntraclassResults executeDescriptor(String fileName) {
		Random random = new Random(5);
		ExecutionPOJO execution;
		try {
			execution = new Gson().fromJson(new FileReader(fileName), ExecutionPOJO.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		IDataReader dataReader = Datasets.createDataset(dataset);
		List<SemanticTrajectory> data = dataReader.read();
		
		Collections.shuffle(data, new java.util.Random() {
			smile.math.Random rnd = new smile.math.Random();
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> similarityCalculators = Measures.createMeasures(measure);
		double bestAUC = 0;
		IntraclassResults ret = null;
		for (TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator : similarityCalculators) {
			BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
			SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
			Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) similarityCalculator);
			
			Stopwatch w = Stopwatch.createStarted();
			if(similarityCalculator instanceof ITrainable) {
				((ITrainable) similarityCalculator).train(Arrays.asList(allData));
			}
			
			Validation.PrecisionAtRecallResults precisionAtRecall = validation.precisionAtRecallWithResult(similarityCalculator, allData, /*data.size() / problemDescriptor.numClasses()*/10);
			w = w.stop();
			System.out.printf("Parameters: '%s'\n", similarityCalculator.parametrization());
			System.out.printf("Elapsed time %d miliseconds\n", w.elapsed(TimeUnit.MILLISECONDS));
			System.out.printf("Precision@recall(%d): %s\n", /*data.size() / problemDescriptor.numClasses()*/10, ArrayUtils.toString(precisionAtRecall, "0.0"));

			DescriptiveStatistics total = new DescriptiveStatistics();
			for (Map.Entry<Object, DescriptiveStatistics> entry : precisionAtRecall.getStats().entrySet()) {
				System.out.printf("%s = %.2f +/- %.2f\n", entry.getKey(), entry.getValue().getMean(), entry.getValue().getStandardDeviation());
				total.addValue(entry.getValue().getMean());
			}
			System.out.printf("Mean intraclass similarity = %.2f\n", total.getMean());
			
			double auc = AUC.precisionAtRecall(precisionAtRecall.getpAtRecall());
			double map = MAP.precisionAtRecall(precisionAtRecall.getpAtRecall());
			System.out.printf("AUC: %.2f\n", auc);
			System.out.printf("MAP: %.2f\n", map);
			if(auc > bestAUC) {
				ret = new IntraclassResults(((IMeasureDistance<SemanticTrajectory>) similarityCalculator).name(), auc, map, total.getMean(), precisionAtRecall.getStats());
				bestAUC = auc;
			}
		}
		return ret;
	}
	
	public static class IntraclassResults {

		private double auc;
		private double map;
		private double mean;
		private Map<Object, DescriptiveStatistics> stats;
		private String measure;

		public IntraclassResults(String measure, double auc, double map, double mean, Map<Object, DescriptiveStatistics> stats) {
			this.measure = measure;
			this.auc = auc;
			this.map = map;
			this.mean = mean;
			this.stats = stats;
		}
		
	}
}
