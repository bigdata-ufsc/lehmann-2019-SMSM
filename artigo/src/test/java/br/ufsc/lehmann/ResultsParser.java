package br.ufsc.lehmann;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.io.LineReader;

@RunWith(Parameterized.class)
public class ResultsParser {

    @Rule public TestName name = new TestName();
	private Path fileName;
	
	private static final Pattern TEST_NAME_PATTERN = Pattern.compile("(.+)#(.+)\\[(.+)\\[(.+)\\]\\[(.+)\\](.+\\[.+\\].+)?\\]");
	private static final Pattern MULTIPLE_MEASURES_PATTERN = Pattern.compile("\\[(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+)\\]");
    
    @Parameters(name="{0}")
    public static Collection<Path> data() throws IOException {
    	List<Path> files = Files.list(Paths.get(new File("./src/test/resources/only-stops").toURI())).filter((Path p) -> {
    		return p.getFileName().toString().endsWith(".out");
    	}).collect(Collectors.toList());
        return files;
    }

	public ResultsParser(Path fileName) {
		this.fileName = fileName;
	}

	@Test
	public void parseClassificationScore() throws Exception {
		LineReader lineReader = new LineReader(new FileReader(fileName.toFile()));
		String line = null;
		Map<String, ExperimentData> datasets = new HashMap<>();
		while((line = lineReader.readLine()) != null) {
			if(line.equals("Executing SQL...")) {
				do {
					line = lineReader.readLine();
				} while(line != null && !TEST_NAME_PATTERN.matcher(line).matches());
				if(line != null) {
					Matcher matcher = TEST_NAME_PATTERN.matcher(line);
					if(matcher.find()) {
						String method = matcher.group(1);
						String test = matcher.group(2);
						String detail = matcher.group(6);
						String dataset = matcher.group(3) + (detail == null ? "" : detail);
						ExperimentData testData = datasets.getOrDefault(dataset, new ExperimentData());
						Experiment exp = testData.addTest(test, method);
						while((line = lineReader.readLine()) != null && line.startsWith("SLF4J")) {
						}
						exp.holdout = line;
						exp.cv = lineReader.readLine();
						datasets.put(dataset, testData);
					}
				}
			}
		}
		File csvOutput = new File(fileName.toFile().getAbsolutePath().toString().replaceFirst("\\.out", ".csv"));
		FileWriter out = new FileWriter(csvOutput);
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL.withDelimiter(';'));
		printer.printRecord("dataset", "measure", "classification-measure", "test-type", "value");
		for (Iterator i = datasets.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, ExperimentData> entry = (Map.Entry<String, ExperimentData>) i.next();
			ExperimentData data = entry.getValue();
			for (Experiment exp : data.tests) {
				Matcher matcher = MULTIPLE_MEASURES_PATTERN.matcher(exp.holdout);
				if(matcher.find()) {
					printer.printRecord(entry.getKey(), exp.method, "precision", "holdout", matcher.group(1));
					printer.printRecord(entry.getKey(), exp.method, "recall", "holdout", matcher.group(2));
					printer.printRecord(entry.getKey(), exp.method, "f-measure", "holdout", matcher.group(3));
					printer.printRecord(entry.getKey(), exp.method, "specificity", "holdout", matcher.group(4));
					printer.printRecord(entry.getKey(), exp.method, "fall-out", "holdout", matcher.group(5));
					printer.printRecord(entry.getKey(), exp.method, "false-discovery-rate", "holdout", matcher.group(6));
				} else {
					printer.printRecord(entry.getKey(), exp.method, "accuracy", "holdout", exp.holdout);
				}
				matcher = MULTIPLE_MEASURES_PATTERN.matcher(exp.cv);
				if(matcher.find()) {
					printer.printRecord(entry.getKey(), exp.method, "precision", "cross-validation", matcher.group(1));
					printer.printRecord(entry.getKey(), exp.method, "recall", "cross-validation", matcher.group(2));
					printer.printRecord(entry.getKey(), exp.method, "f-measure", "cross-validation", matcher.group(3));
					printer.printRecord(entry.getKey(), exp.method, "specificity", "cross-validation", matcher.group(4));
					printer.printRecord(entry.getKey(), exp.method, "fall-out", "cross-validation", matcher.group(5));
					printer.printRecord(entry.getKey(), exp.method, "false-discovery-rate", "cross-validation", matcher.group(6));
				} else {
					printer.printRecord(entry.getKey(), exp.method, "accuracy", "cross-validation", exp.cv);
				}
			}
		}
		printer.flush();
		printer.close();
	}

	@Test
	public void parseExecutionTime() throws Exception {
		
	}
	
	private static class ExperimentData {
		List<Experiment> tests = new ArrayList<>();

		public Experiment addTest(String test, String method) {
			Experiment e = new Experiment(test, method);
			tests.add(e);
			return e;
		}
		
		@Override
		public String toString() {
			return tests.toString();
		}
	}
	
	private static class Experiment {

		private String test;
		private String method;
		private String holdout;
		private String cv;
		public Experiment(String test, String method) {
			this.test = test;
			this.method = method;
		}
		@Override
		public String toString() {
			return "Experiment [test=" + test + ", method=" + method + ", holdout=" + holdout + ", cv=" + cv + "]";
		}
	}
}
