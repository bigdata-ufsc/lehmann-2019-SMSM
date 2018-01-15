package br.ufsc.lehmann;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

public class FilterBetterPrecisionAtRecallResultPerMeasure {

	private Path fileName;
	
	private static final Pattern TEST_NAME_PATTERN = Pattern.compile("(.+)#(.+)\\[(.+)\\[(.+)\\]\\[(.+)\\](.+\\[.+\\].+)?\\]");
	private static final Pattern MULTIPLE_MEASURES_PATTERN = Pattern.compile("\\[(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+).*,.*(\\d+\\.\\d+)\\]");
	private static final Pattern PRECISION_AT_RECALL_PATTERN = Pattern.compile("Precision@recall\\((\\d+)\\): \\{(.+)\\}");

    public static Collection<Path> individualResults() throws IOException {
    	List<Path> files = Files.list(Paths.get(new File("./src/main/resources/only-stops-dataset").toURI())).filter((Path p) -> {
    		return p.getFileName().toString().endsWith("Proportion-pr.csv");
    	}).collect(Collectors.toList());
        return files;
    }

	@Test
	public void filteringResults() throws Exception {
		Collection<Path> results = individualResults();
		Map<String, BetterResult> betterResults = new HashMap<>();
		for (Path filePath : results) {
			CSVParser parser = new CSVParser(//
					new FileReader(filePath.toFile()),// 
					CSVFormat.EXCEL.withDelimiter(',').withHeader("measure","dataset","0%","\"10,00%\""));
			for (CSVRecord csvRecord : parser) {
				if(csvRecord.get("dataset").equals("dataset")) {
					continue;
				}
				String key = csvRecord.get("measure") + "." + csvRecord.get("dataset");
				String valuesStr = csvRecord.get("\"10,00%\"");
				String[] values = valuesStr.split(",");
				Double map = Arrays.asList(values).stream().mapToDouble((s) -> Double.parseDouble(s)).average().getAsDouble();
				betterResults.computeIfAbsent(key, (e) -> new BetterResult(map, values, filePath, csvRecord)).setValues(map, values, filePath, csvRecord);
			}
		}
		File csvOutput = new File(new File("./src/main/resources/only-stops-dataset"), "better-results.csv");
		if(csvOutput.exists()) {
			csvOutput.delete();
		}
		FileWriter out = new FileWriter(csvOutput);
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL//
				.withDelimiter(',')//
				.withHeader("measure","dataset","0.0","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0"));
		List<String> orderedNames = new ArrayList<>(betterResults.keySet()).stream().sorted().collect(Collectors.toList());
		for (Iterator iterator = orderedNames.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			BetterResult value = betterResults.get(string);
			printer.printRecord(//
					value.getCsv().get("measure").trim(), value.getCsv().get("dataset").trim(), 
					"1.0", 
					value.data[0], 
					value.data[1], 
					value.data[2], 
					value.data[3], 
					value.data[4], 
					value.data[5], 
					value.data[6], 
					value.data[7], 
					value.data[8], 
					value.data[9]);
		}
		printer.flush();
		printer.close();
	}
	
	static class BetterResult {
		Double map;
		String[] data;
		Path file;
		CSVRecord csv;
		@Override
		public String toString() {
			return "BetterResult [map=" + map + ", data=" + data + "]";
		}
		public BetterResult(Double map, String[] data, Path file, CSVRecord csv) {
			super();
			this.map = map;
			this.data = data;
			this.file = file;
			this.csv = csv;
		}
		public void setValues(Double map, String[] data, Path file, CSVRecord csv) {
			if(map > this.map) {
				this.map = map;
				this.data = data;
				this.file = file;
				this.csv = csv;
			}
		}
		public CSVRecord getCsv() {
			return csv;
		}
		public void setCsv(CSVRecord csv) {
			this.csv = csv;
		}
		public Double getMap() {
			return map;
		}
		public void setMap(Double map) {
			this.map = map;
		}
		public String[] getData() {
			return data;
		}
		public void setData(String[] data) {
			this.data = data;
		}
		public Path getFile() {
			return file;
		}
		public void setFile(Path file) {
			this.file = file;
		}
	}
}
