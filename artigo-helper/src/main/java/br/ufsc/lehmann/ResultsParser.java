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

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.LineReader;

@RunWith(Parameterized.class)
public class ResultsParser {

    @Rule public TestName name = new TestName();
	private Path fileName;
	
	private static final Pattern TEST_NAME_PATTERN = Pattern.compile("(.+)#(.+)\\[(.+)\\[(.+)\\]\\[(.+)\\](.+\\[.+\\].+)?\\]");
	private static final Pattern MULTIPLE_MEASURES_PATTERN = Pattern.compile("\\[(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+)\\]");
	private static final Pattern PRECISION_AT_RECALL_PATTERN = Pattern.compile("Precision@recall\\((\\d+)\\): \\{(.+)\\}");
    
    @Parameters(name="{0}")
    public static Collection<Path> data() throws IOException {
    	List<Path> files = Files.list(Paths.get(new File("./src/main/resources/only-stops-dataset").toURI())).filter((Path p) -> {
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
//			if(line.equals("Reading file...")) {
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
						String stopSemantic = matcher.group(4);
						String dataset = matcher.group(3) + (detail == null ? "" : detail);
						ExperimentData testData = datasets.getOrDefault(dataset, new ExperimentData());
						Experiment exp = testData.addTest(test, method, stopSemantic);
						while((line = lineReader.readLine()) != null && line.startsWith("SLF4J")) {
						}
						Matcher m = PRECISION_AT_RECALL_PATTERN.matcher(line);
						if(m.find()) {
							exp.pr_level = Integer.parseInt(m.group(1));
							exp.pr_curve = m.group(2);
						} else {
							exp.holdout = line;
							exp.cv = lineReader.readLine();
						}
						datasets.put(dataset, testData);
					}
				}
			}
		}
		CSVData csv = new CSVData();
		for (Iterator i = datasets.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, ExperimentData> entry = (Map.Entry<String, ExperimentData>) i.next();
			ExperimentData data = entry.getValue();
			for (Experiment exp : data.tests) {
				if(exp.holdout != null) {
					Matcher matcher = MULTIPLE_MEASURES_PATTERN.matcher(exp.holdout);
					if(matcher.find()) {
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "precision", "holdout", matcher.group(1));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "recall", "holdout", matcher.group(2));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "f-measure", "holdout", matcher.group(3));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "specificity", "holdout", matcher.group(4));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "fall-out", "holdout", matcher.group(5));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "false-discovery-rate", "holdout", matcher.group(6));
					} else {
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "accuracy", "holdout", exp.holdout);
					}
				}
				if(exp.cv != null) {
					Matcher matcher = MULTIPLE_MEASURES_PATTERN.matcher(exp.cv);
					if(matcher.find()) {
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "precision", "cross-validation", matcher.group(1));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "recall", "cross-validation", matcher.group(2));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "f-measure", "cross-validation", matcher.group(3));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "specificity", "cross-validation", matcher.group(4));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "fall-out", "cross-validation", matcher.group(5));
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "false-discovery-rate", "cross-validation", matcher.group(6));
					} else {
						csv.add(entry.getKey(), exp.stopSemantic, exp.method, "accuracy", "cross-validation", exp.cv);
					}
				}
			}
		}
		if(!csv.isEmpty()) {
			File csvOutput = new File(fileName.toFile().getAbsolutePath().toString().replaceFirst("\\.out", ".csv"));
			FileWriter out = new FileWriter(csvOutput);
			CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL.withDelimiter(';').withHeader("dataset", "stop-semantic", "measure", "test-type", "Accuracy", "Precision", "Recall", "F-Measure", "Specificity", "Fall-out", "FDR"));
			csv.printTo(printer);
			printer.flush();
			printer.close();
		}

		int bands = 0;
		PrecisionRecallData pr = new PrecisionRecallData();
		for (Iterator i = datasets.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, ExperimentData> entry = (Map.Entry<String, ExperimentData>) i.next();
			ExperimentData data = entry.getValue();
			for (Experiment exp : data.tests) {
				if(exp.pr_curve != null) {
					bands = exp.pr_level;
					pr.add(exp.method, entry.getKey(), exp.pr_level, exp.pr_curve);
				}
			}
		}
		
		if(!pr.isEmpty()) {
			File prCurveOutput = new File(fileName.toFile().getAbsolutePath().toString().replaceFirst("\\.out", "-pr.csv"));
			FileWriter prCurve = new FileWriter(prCurveOutput);
			List<String> columns = new ArrayList<>(bands + 3);
			columns.add("measure");
			columns.add("dataset");
			columns.add("0%");
			for (int i = 0; i < bands; i++) {
				columns.add(String.format("%.2f", ((i + 1) * (100.0 / bands))) + "%");
			}
			CSVPrinter printer = new CSVPrinter(prCurve, CSVFormat.EXCEL.withDelimiter(',').withHeader(columns.toArray(new String[columns.size()])));
			pr.printTo(printer);
			printer.flush();
			printer.close();
		}
	}

	@Test
	public void parseExecutionTime() throws Exception {
		
	}
	
	private static class PrecisionRecallData {

		Multimap<String, CSVMeasureData> data = MultimapBuilder.hashKeys().arrayListValues().build();

		public void add(String measure, String dataset, Integer recallLevel, String value) {
			data.put(measure + "<&>" + dataset + "<&>" + recallLevel, new CSVMeasureData(String.valueOf(recallLevel), value));
		}
		public boolean isEmpty() {
			return data.isEmpty();
		}
		public void printTo(CSVPrinter printer) {
			data.asMap().forEach((String key, Collection<CSVMeasureData> measures) -> {
				List<String> values = new ArrayList<>();
				String[] split = key.split("<&>");
				values.add(split[0]);
				values.add(split[1]);
				values.add(split[2]);
				for (CSVMeasureData measureData : measures) {
					values.add(measureData.value);
				}
				try {
					printer.printRecord(values.toArray(new String[values.size()]));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
	
	private static class CSVData {
		Multimap<String, CSVMeasureData> data = MultimapBuilder.hashKeys().arrayListValues().build();
		
		public void add(String dataset, String stopSemantic, String method, String measure, String testType, String value) {
			data.put(dataset + "<&>" + stopSemantic+ "<&>" + method+ "<&>" +  testType, new CSVMeasureData(measure, value));
		}

		public boolean isEmpty() {
			return data.isEmpty();
		}

		public void printTo(CSVPrinter printer) {
			data.asMap().forEach((String key, Collection<CSVMeasureData> measures) -> {
				Map<String, String> values = new HashMap<>();
				for (CSVMeasureData measureData : measures) {
					values.put(measureData.measure, measureData.value);
				}
				String[] split = key.split("<&>");
				String dataset = split[0];
				String stopSemantic = split[1];
				String measure = split[2];
				String type = split[3];
				try {
					printer.printRecord(dataset.trim(), stopSemantic.trim(), measure.trim(), type.trim(), values.get("accuracy"), values.get("precision"), values.get("recall"), values.get("f-measure"), values.get("specificity"), values.get("fall-out"), values.get("false-discovery-rate"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
	
	private static class CSVMeasureData {
		private String measure;
		private String value;

		public CSVMeasureData(String measure, String value) {
			this.measure = measure;
			this.value = value;
		}
	}
	
	private static class ExperimentData {
		List<Experiment> tests = new ArrayList<>();

		public Experiment addTest(String test, String method, String stopSemantic) {
			Experiment e = new Experiment(test, method, stopSemantic);
			tests.add(e);
			return e;
		}
		
		@Override
		public String toString() {
			return tests.toString();
		}
	}
	
	private static class Experiment {

		private int pr_level;
		private String pr_curve;
		private String test;
		private String method;
		private String stopSemantic;
		private String holdout;
		private String cv;
		public Experiment(String test, String method, String stopSemantic) {
			this.test = test;
			this.method = method;
			this.stopSemantic = stopSemantic;
		}
		@Override
		public String toString() {
			return "Experiment [test=" + test + ", method=" + method + ", stopSemantic=" + stopSemantic + ", holdout="
					+ holdout + ", cv=" + cv + ", p@r(" + pr_level + ")=]" + pr_curve;
		}
	}
}
