package br.ufsc.lehmann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.ITrainable;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;

public class PerformanceTopK {


	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		Stream<java.nio.file.Path> files = java.nio.file.Files.walk(Paths.get("./src/test/resources/performance/"));
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "6");
		files.filter(path -> path.toFile().isFile() && path.toString().endsWith(".test")).forEach(path -> {
			String fileName = path.toString();
			System.out.printf("Executing file %s\n", fileName);
			
			PrintStream  bkp = System.out;
			try {
				ExecutionPOJO execution;
				try {
					System.setOut(new PrintStream(new FileOutputStream(new File(path.toFile().getParentFile(), path.getFileName().toString() + ".out"))));
					execution = new Gson().fromJson(new FileReader(fileName), ExecutionPOJO.class);
				} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
					throw new RuntimeException(e);
				}
				Dataset dataset = execution.getDataset();
				Measure measure = execution.getMeasure();
				IDataReader dataReader = Datasets.createDataset(dataset);
				List<SemanticTrajectory> originalData = dataReader.read();
				
				DescriptiveStatistics sizeStats = new DescriptiveStatistics();
				originalData.stream().forEach(t -> sizeStats.addValue(t.length()));
				List<SemanticTrajectory> data = originalData
						.stream()
						.filter(t -> (t.length() >= (sizeStats.getMean() - (3 * sizeStats.getStandardDeviation()))) && 
								(t.length() <= (sizeStats.getMean() + (3 * sizeStats.getStandardDeviation()))))
						.collect(Collectors.toList());
				
				System.out.println("New dataset size: " + data.size());
				
				DescriptiveStatistics stats = new DescriptiveStatistics();
				data.stream().forEach(t -> stats.addValue(t.length()));
				System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
				
				IntStream.of(1_000, 2_000, 3_160, 5_000, 7_070, 10_000  ).forEach(base -> executeDescriptor(measure, data, base));
			} finally {
				System.setOut(bkp);
			}
		});
		files.close();
	}

	private static void executeDescriptor(Measure measure, Collection<SemanticTrajectory> originalData, int base) {
		List<SemanticTrajectory> data = new ArrayList<>(base);
		List<SemanticTrajectory> d = originalData.stream().limit(base).collect(Collectors.toList());
		float nextUp = Math.nextUp(base / d.size()) + 1;
		IntStream.range(0, (int) nextUp).forEach(i -> data.addAll(d));
		List<SemanticTrajectory> finalData = data.stream().limit(base).collect(Collectors.toList());
		
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);

		SemanticTrajectory[] allData = finalData.toArray(new SemanticTrajectory[finalData.size()]);
		
		Stopwatch w = Stopwatch.createStarted();
		if(similarityCalculator instanceof ITrainable) {
			((ITrainable) similarityCalculator).train(Arrays.asList(allData));
		}

		for (SemanticTrajectory t1 : allData) {
			for (SemanticTrajectory t2 : allData) {
				similarityCalculator.getSimilarity(t1, t2);
			}
		}
		w = w.stop();
		System.out.printf("[%d] Elapsed time %d miliseconds\n", base, w.elapsed(TimeUnit.MILLISECONDS));
	}
}
