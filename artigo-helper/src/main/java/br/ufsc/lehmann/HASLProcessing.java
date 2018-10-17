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
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import smile.data.parser.IOUtils;

public class HASLProcessing {

	public static void main(String[] args) throws IOException {
		Stream<Path> files = Files.walk(Paths.get("./src/main/resources/High Quality ASL"));
		List<Word> words = new ArrayList<>();
		files.forEach(p -> {
			if(p.toFile().isFile()) {
				List<Line> lines = new ArrayList<>();
				List<String> fileLines = null;
				try {
					fileLines = IOUtils.readLines(new FileReader(p.toFile()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				fileLines.stream().filter(line -> !line.isEmpty()).forEach(line -> lines.add(new Line(line.split("\t"))));
				words.add(new Word(lines, p.toFile().getName(), p.toFile().getParentFile().getName()));
			}
		});
		files.close();
		StringBuilder out = new StringBuilder();
		CSVPrinter csv = new CSVPrinter(out, CSVFormat.DEFAULT.withDelimiter(';'));
		csv.printRecord("gid", "tid", "author", "class", "lx", "ly", "lz", "lroll", "lpitch", "lyaw", "lthumb", "lfore", "lmiddle", "lring", "llittle", "rx", "ry", "rz", "rroll", "rpitch", "ryaw", "rthumb", "rfore", "rmiddle", "rring", "rlittle");
		MutableInt gid = new MutableInt();
		MutableInt tid = new MutableInt();
		words.stream().forEach(w -> {
			ArrayList<Object> tokens = new ArrayList<>(30);
			w.lines.stream().forEach(line -> {
				try {
					tokens.clear();
					tokens.add(gid.getAndIncrement());
					tokens.add(tid.getValue());
					tokens.add(w.author);
					tokens.add(w.word.substring(0, w.word.indexOf('-')));
					tokens.addAll(Arrays.asList(line.fields));
					csv.printRecord(tokens);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			tid.increment();
		});
		csv.flush();
		csv.close();
		FileWriter writer = new FileWriter(Paths.get("./src/main/resources/hasl.csv").toFile());
		writer.write(out.toString());
		writer.flush();
		writer.close();
	}
	
	private static class Line {
		String[] fields;

		public Line(String[] fields) {
			this.fields = fields;
		}
	}
	
	private static class Word {
		List<Line> lines;
		String word;
		String author;
		public Word(List<Line> lines, String word, String author) {
			this.lines = lines;
			this.word = word;
			this.author = author;
		}
	}
}
