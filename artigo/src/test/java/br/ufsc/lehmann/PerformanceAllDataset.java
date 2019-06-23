package br.ufsc.lehmann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

public class PerformanceAllDataset {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		for (int j = 0; j < 1; j++) {
			System.out.println((j + 1) + "º execution");
			Stream<java.nio.file.Path> files = java.nio.file.Files.walk(Paths.get("./src/test/resources/performance/"));

			files.filter(path -> path.toFile().isFile() &&
					path.toFile().getParentFile().getName().equalsIgnoreCase("raw") && 
					path.toString().endsWith(".test")).forEach(path -> {
						String fileName = path.toString();
//						if(!(fileName.contains("EDR") || fileName.contains("LCSS"))) {
//							return;
//						}
						File out = new File(fileName + ".out");
						int i = 1;
						while(out.exists()) {
							out = new File(fileName + i++ + ".out");
						}
						if(out.exists()) {
//							return;
						}
						System.out.printf("Executing file %s\n", fileName);
						PrintStream bkp = System.out;
						try {
							ExecutionPOJO execution;
							try {
								execution = new Gson().fromJson(new FileReader(fileName), ExecutionPOJO.class);
								System.setOut(new PrintStream(new FileOutputStream(out)));
							} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
								throw new RuntimeException(e);
							}
							Dataset dataset = execution.getDataset();
							Measure measure = execution.getMeasure();
							IDataReader dataReader = Datasets.createDataset(dataset);
							List<SemanticTrajectory> data = dataReader.read();

							executeDescriptor(measure, data);
						} finally {
							System.setOut(bkp);
						}
					});
			files.close();
		}
	}

	private static void executeDescriptor(Measure measure, Collection<SemanticTrajectory> originalData) {

		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);

		SemanticTrajectory[] allData = originalData.toArray(new SemanticTrajectory[originalData.size()]);

		int count = 0, progress = 0;
		int size = allData.length;
		double centesimalPart = size / 100.0;
		Stopwatch w = Stopwatch.createStarted();
		if (similarityCalculator instanceof ITrainable) {
			((ITrainable) similarityCalculator).train(Arrays.asList(allData));
		}

		for (SemanticTrajectory t1 : allData) {
			count++;
			for (SemanticTrajectory t2 : allData) {
				similarityCalculator.getSimilarity(t1, t2);
			}
			if(count % centesimalPart > centesimalPart - 1) {
				System.out.print(++progress + "% - ");
				if(progress == 5) {
					break;
				}
				if(progress % 25 == 24) {
					System.out.println();
				}
			}
		}
		w = w.stop();
		System.out.printf("\r\nElapsed time %d miliseconds\n", w.elapsed(TimeUnit.MILLISECONDS) * 20);
	}
}
