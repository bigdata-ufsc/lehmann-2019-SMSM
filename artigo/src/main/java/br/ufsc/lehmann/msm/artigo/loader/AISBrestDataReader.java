package br.ufsc.lehmann.msm.artigo.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.DatasetDescriptor;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import cc.mallet.util.IoUtils;

public class AISBrestDataReader implements IDataReader {
	
	private static int semantics_count = 3;
	
	public static final BasicSemantic<Integer> SHIPCODE = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> HEADING = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> SPEED = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> COG = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> ROT = new BasicSemantic<>(semantics_count++);

	private static final DateFormat DF = new SimpleDateFormat("YYYY-MM-DD HH:mm:SS");

	@Override
	public List<SemanticTrajectory> read() {
		try {
			return readRawFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<SemanticTrajectory> readRawFile() throws IOException, UnsupportedEncodingException {
		System.out.println("Reading file...");
		File file = new File("./src/main/resources/datasets/AIS/AIS_descriptor.json");
		DatasetDescriptor descriptor = new Gson().fromJson(new FileReader(file), DatasetDescriptor.class);
		Multimap<Integer, AISBrestRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		for (String csvFile : descriptor.getData_files()) {
			ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(new File(file.getParentFile(), csvFile).getAbsolutePath(), "UTF-8"));
			InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(csvFile.substring(0, csvFile.lastIndexOf('.')) + ".csv")));
			CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
					CSVFormat.EXCEL.withHeader(descriptor.getHeader().toArray(new String[descriptor.getHeader().size()])).withDelimiter(';'));
			Iterator<CSVRecord> pointsData = pointsParser.iterator();
			while(pointsData.hasNext()) {
				CSVRecord data = pointsData.next();
				if(data.getRecordNumber() == 1) {
					//skip header
					continue;
				}
				int tid = Integer.parseInt(data.get("tid"));
				Timestamp date = null;
				try {
					date = new Timestamp(DF.parse(StringUtils.defaultIfBlank(data.get("date"), null)).getTime());
				} catch (ParseException e) {
					System.err.println(e.getMessage());
				}
				TPoint latlon = new TPoint(Double.parseDouble(data.get("lon")), Double.parseDouble(data.get("lat")), date);
				AISBrestRecord record = new AISBrestRecord(
						Integer.parseInt(data.get("gid")),
						tid,
						Integer.parseInt(data.get("shipcode")),
						date,
						Double.parseDouble(data.get("heading")),
						Double.parseDouble(data.get("speed")),
						Double.parseDouble(data.get("cog")),
						Double.parseDouble(data.get("rot")),
						latlon
						);
				records.put(record.getTid(), record);
			}
			zipFile.close();
		}
		List<Integer> toRemove = records.asMap().entrySet().stream().filter(entry -> entry.getValue().size() == 1).map(entry -> entry.getKey()).collect(Collectors.toList());
		toRemove.forEach(id -> records.removeAll(id));
		
		System.out.printf("Loaded %d points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics trajLenghtStats = new DescriptiveStatistics();
		DescriptiveStatistics trajSamplingStats = new DescriptiveStatistics();
		DescriptiveStatistics trajPointStats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantics_count);
			Collection<AISBrestRecord> collection = records.get(trajId);
			collection = collection.stream().sorted(new Comparator<AISBrestRecord>() {

				@Override
				public int compare(AISBrestRecord o1, AISBrestRecord o2) {
					return o1.getDate().compareTo(o2.getDate());
				}
			}).collect(Collectors.toList());
			int i = 0;
			DescriptiveStatistics samplingStats = new DescriptiveStatistics();
			DescriptiveStatistics pointsStats = new DescriptiveStatistics();
			Instant previousInstant = null;
			TPoint previousPoint = null;
			for (AISBrestRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL_EUCLIDEAN, record.getLatlon());
				Instant instant = Instant.ofEpochMilli(record.getDate().getTime());
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(instant, instant));
				s.addData(i, SHIPCODE, record.getShipcode());
				s.addData(i, HEADING, record.getHeading());
				s.addData(i, SPEED, record.getSpeed());
				s.addData(i, COG, record.getCog());
				s.addData(i, ROT, record.getRot());
				i++;
				if(previousInstant != null) {
					samplingStats.addValue(previousInstant.until(instant, ChronoUnit.SECONDS) / 60.0);
				}
				if(previousPoint != null) {
					pointsStats.addValue(Semantic.SPATIAL_EUCLIDEAN.distance(previousPoint, record.getLatlon()));
				}
				previousInstant = instant;
				previousPoint = record.getLatlon();
			}
			trajLenghtStats.addValue(s.length());
			trajSamplingStats.addValue(samplingStats.getMean());
			trajPointStats.addValue(pointsStats.getMean());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f, sampling rate - %.2f points per minute, mean distance between point - %.2f\n", 
				trajLenghtStats.getMean(), trajLenghtStats.getMin(), trajLenghtStats.getMax(), trajLenghtStats.getStandardDeviation(), 1 / trajSamplingStats.getPercentile(50), trajPointStats.getPercentile(50));
		return ret;
	}

	public static void main(String[] args) {
		List<SemanticTrajectory> read = new AISBrestDataReader().read();
		Multimap<Integer, SemanticTrajectory> classes = MultimapBuilder.hashKeys().arrayListValues().build();
		for (SemanticTrajectory t : read) {
			classes.put(SHIPCODE.getData(t, 0), t);
		}
		read = null;
		Set<Integer> keys = classes.keySet();
		for (Integer k : keys) {
			Collection<SemanticTrajectory> trajs = classes.get(k);
			System.out.printf("\tSHIPCODE '%d' - %d trajectories\n", k, trajs.size());
		}
	}
}
