package br.ufsc.lehmann.msm.artigo.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import cc.mallet.util.IoUtils;

public class AnimalsSTARKEYDataReader implements IDataReader {
	
	private static int semantics_count = 3;
	
	public static final BasicSemantic<String> SPECIES = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Date> GRENSUNR = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Date> GRENSUNS = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> OBSWT = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Long> STARKEY_TIME = new BasicSemantic<>(semantics_count++);

	private static final DateFormat DF = new SimpleDateFormat("YYYYMMDD HH:mm:SS");
	private static final DateFormat TF = new SimpleDateFormat("HH:mm:SS");

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
		Multimap<String, AnimalRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		String file = "./datasets/animal.starkey.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(file).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("animal.starkey.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("UTMGrid", "UTMGridEast", "UTMGridNorth", "Id", "StarkeyTime", "GMDate", "GMTime", "LocDate", "LocTime", "RadNum", "Species", "UTME", "UTMN", "Year", "Grensunr", "Grensuns", "Obswt").withDelimiter(';'));
		Iterator<CSVRecord> pointsData = pointsParser.iterator();
		int gid = 0;
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(data.getRecordNumber() == 1) {
				//skip header
				continue;
			}
			String tid = data.get("Id");
			Timestamp date = null;
			Date grensunr = null;
			Date grensuns = null;
			try {
				date = new Timestamp(DF.parse(data.get("LocDate") + " " + data.get("LocTime")).getTime());
				grensunr = TF.parse(data.get("Grensunr"));
				grensuns = TF.parse(data.get("Grensuns"));
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			}
			TPoint latlon = new TPoint(Double.parseDouble(data.get("UTMGridEast")), Double.parseDouble(data.get("UTMGridNorth")), date);
			AnimalRecord record = new AnimalRecord(
					gid++,
					tid,
					latlon,
					date,
					data.get("Species"),
					grensunr,
					grensuns,
					Double.parseDouble(data.get("Obswt")),
					Long.parseLong(data.get("StarkeyTime"))
					);
			records.put(record.getTid(), record);
		}
		zipFile.close();
		System.out.printf("Loaded %d points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics trajLenghtStats = new DescriptiveStatistics();
		DescriptiveStatistics trajSamplingStats = new DescriptiveStatistics();
		DescriptiveStatistics trajPointStats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantics_count);
			Collection<AnimalRecord> collection = records.get(trajId);
			collection = collection.stream().sorted(new Comparator<AnimalRecord>() {

				@Override
				public int compare(AnimalRecord o1, AnimalRecord o2) {
					return Long.compare(o1.getStarkeyTime(), o2.getStarkeyTime());
				}
			}).collect(Collectors.toList());
			DescriptiveStatistics samplingStats = new DescriptiveStatistics();
			DescriptiveStatistics pointsStats = new DescriptiveStatistics();
			Instant previousInstant = null;
			TPoint previousPoint = null;
			int i = 0;
			for (AnimalRecord record : collection) {
				Instant instant = Instant.ofEpochMilli(Date.parse("12/31/1987") + record.getStarkeyTime() * 1000);
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = record.getLatlon();
				s.addData(i, Semantic.SPATIAL_EUCLIDEAN, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(instant, instant));
				s.addData(i, SPECIES, record.getSpecie());
				s.addData(i, GRENSUNR, record.getGrensunr());
				s.addData(i, GRENSUNS, record.getGrensuns());
				s.addData(i, OBSWT, record.getObswt());
				s.addData(i, STARKEY_TIME, record.getStarkeyTime());
				i++;
				if(previousInstant != null) {
					double v = previousInstant.until(instant, ChronoUnit.SECONDS) / (60.0);
					samplingStats.addValue(v);
				}
				if(previousPoint != null) {
					double distance = Semantic.SPATIAL_EUCLIDEAN.distance(previousPoint, point);
					pointsStats.addValue(distance);
				}
				previousInstant = instant;
				previousPoint = point;
			}
			trajLenghtStats.addValue(s.length());
			trajSamplingStats.addValue(samplingStats.getPercentile(50));
			trajPointStats.addValue(pointsStats.getPercentile(50));
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f, sampling rate - %.10f points per minute, mean distance between point - %.2f\n", trajLenghtStats.getMean(), trajLenghtStats.getMin(), trajLenghtStats.getMax(), trajLenghtStats.getStandardDeviation(), 1 / trajSamplingStats.getMean(), trajPointStats.getMean());
		return ret;
	}

	public static void main(String[] args) {
		List<SemanticTrajectory> read = new AnimalsSTARKEYDataReader().read();
		Multimap<String, SemanticTrajectory> classes = MultimapBuilder.hashKeys().arrayListValues().build();
		for (SemanticTrajectory t : read) {
			classes.put(SPECIES.getData(t, 0), t);
		}
		read = null;
		Set<String> keys = classes.keySet();
		for (String k : keys) {
			Collection<SemanticTrajectory> trajs = classes.get(k);
			System.out.printf("\tSPECIES '%s' - %d trajectories\n", k, trajs.size());
		}
	}
}
