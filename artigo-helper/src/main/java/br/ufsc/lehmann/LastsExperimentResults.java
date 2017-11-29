package br.ufsc.lehmann;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

public class LastsExperimentResults {
    
    @Test
	public void merge() throws Exception {
		Collection<Path> paths = data();
		Map<ExperimentDescriptor, String[]> data = new HashMap<>();
		for (Path path : paths) {
			CSVParser parser = new CSVParser(//
					new FileReader(path.toFile()),// 
					CSVFormat.EXCEL.withDelimiter(';').withHeader("dataset", "stop-semantic", "measure", "test-type", "Accuracy", "Precision", "Recall", "F-Measure", "Specificity", "Fall-out", "FDR"));
			for (CSVRecord csvRecord : parser) {
				if(csvRecord.get("dataset").equals("dataset")) {
					continue;
				}
				data.put(new ExperimentDescriptor(csvRecord.get("dataset"),csvRecord.get("stop-semantic"), 
						csvRecord.get("measure"), csvRecord.get("test-type")), 
						new String[]{csvRecord.get("Accuracy"),
								csvRecord.get("Precision"),
								csvRecord.get("Recall"),
								csvRecord.get("F-Measure"),
								csvRecord.get("Specificity"),
								csvRecord.get("Fall-out"),
								csvRecord.get("FDR")
								});
			}
			parser.close();
		}
		File csvOutput = new File(new File("./src/main/resources/only-stops-dataset"), "aggregated-results.csv");
		if(csvOutput.exists()) {
			csvOutput.delete();
		}
		FileWriter out = new FileWriter(csvOutput);
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL//
				.withDelimiter(';')//
				.withHeader("test-type", "dataset", "stop-semantic", "measure", "Accuracy", "Precision", "Recall", "F-Measure", "Specificity", "Fall-out", "FDR"));
		List<ExperimentDescriptor> orderedExperiments = data.keySet()//
				.stream()//
				.sorted(Comparator.comparing(ExperimentDescriptor::getTestType).thenComparing(ExperimentDescriptor::getDataset).thenComparing(ExperimentDescriptor::getStopSemantic).thenComparing(ExperimentDescriptor::getMeasure))//
				.collect(Collectors.toList());
		for (ExperimentDescriptor exp: orderedExperiments) {
			printer.printRecord(//
					exp.testType, exp.dataset, exp.stopSemantic, exp.measure, // 
					data.get(exp)[0], data.get(exp)[1], data.get(exp)[2], data.get(exp)[3], data.get(exp)[4], data.get(exp)[5], data.get(exp)[6]);
		}
		printer.flush();
		printer.close();
	}

    private static Collection<Path> data() throws IOException {
    	List<Path> files = Files.list(Paths.get(new File("./src/main/resources/only-stops-dataset").toURI())).filter((Path p) -> {
    		return !p.getFileName().toString().equals("aggregated-results.csv") && p.getFileName().toString().endsWith(".csv");
    	}).sorted((Path p1, Path p2) -> p1.compareTo(p2)).collect(Collectors.toList());
        return files;
    }
    
    private static class ExperimentDescriptor {
    	String dataset;
    	String measure;
    	String testType;
		String stopSemantic;
		public ExperimentDescriptor(String dataset, String stopSemantic, String measure, String testType) {
			this.dataset = dataset;
			this.stopSemantic = stopSemantic;
			this.measure = measure;
			this.testType = testType;
		}
		public String getDataset() {
			return dataset;
		}
		public String getStopSemantic() {
			return stopSemantic;
		}
		public String getMeasure() {
			return measure;
		}
		public String getTestType() {
			return testType;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
			result = prime * result + ((measure == null) ? 0 : measure.hashCode());
			result = prime * result + ((stopSemantic == null) ? 0 : stopSemantic.hashCode());
			result = prime * result + ((testType == null) ? 0 : testType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExperimentDescriptor other = (ExperimentDescriptor) obj;
			if (dataset == null) {
				if (other.dataset != null)
					return false;
			} else if (!dataset.equals(other.dataset))
				return false;
			if (measure == null) {
				if (other.measure != null)
					return false;
			} else if (!measure.equals(other.measure))
				return false;
			if (stopSemantic == null) {
				if (other.stopSemantic != null)
					return false;
			} else if (!stopSemantic.equals(other.stopSemantic))
				return false;
			if (testType == null) {
				if (other.testType != null)
					return false;
			} else if (!testType.equals(other.testType))
				return false;
			return true;
		}
    }
}
