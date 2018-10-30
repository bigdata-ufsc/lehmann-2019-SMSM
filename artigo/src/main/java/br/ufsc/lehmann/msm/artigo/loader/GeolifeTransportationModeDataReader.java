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
import br.ufsc.lehmann.msm.artigo.problems.GeolifeRecord;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import cc.mallet.util.IoUtils;

public class GeolifeTransportationModeDataReader implements IDataReader {
	
	private static int semantics_count = 3;
	
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<String> MODE = new BasicSemantic<>(semantics_count++);

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
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		String file = "./datasets/geolife_transportation_mode.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(file).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("geolife_transportation_mode.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				
				CSVFormat.EXCEL.withHeader("gid", "tid", "lat", "lon", "timestamp", "altitude", "user_id", "mode").withDelimiter(';'));
		Iterator<CSVRecord> pointsData = pointsParser.iterator();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(data.getRecordNumber() == 1) {
				//skip header
				continue;
			}
			int tid = Integer.parseInt(data.get("tid"));
			int gid = Integer.parseInt(data.get("gid"));
			Timestamp date = null;
			try {
				date = new Timestamp(DF.parse(data.get("timestamp")).getTime());
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			}
			TPoint latlon = new TPoint(Double.parseDouble(data.get("lon")), Double.parseDouble(data.get("lat")), date);
			GeolifeRecord record = new GeolifeRecord(
					tid,
					gid,
					date,
					latlon.getX(),
					latlon.getY(),
					Integer.parseInt(data.get("user_id")),
					data.get("mode")
					);
			records.put(record.getTid(), record);
		}
		zipFile.close();
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
			Collection<GeolifeRecord> collection = records.get(trajId);
			collection = collection.stream().sorted(new Comparator<GeolifeRecord>() {

				@Override
				public int compare(GeolifeRecord o1, GeolifeRecord o2) {
					return o1.getTime().compareTo(o2.getTime());
				}
			}).collect(Collectors.toList());
			DescriptiveStatistics samplingStats = new DescriptiveStatistics();
			DescriptiveStatistics pointsStats = new DescriptiveStatistics();
			Instant previousInstant = null;
			TPoint previousPoint = null;
			int i = 0;
			for (GeolifeRecord record : collection) {
				TPoint point = new TPoint(record.getLongitude(), record.getLatitude(), record.getTime());
				Instant instant = Instant.ofEpochMilli(record.getTime().getTime());
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL_EUCLIDEAN, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(instant, instant));
				s.addData(i, USER_ID, record.getUserId());
				s.addData(i, MODE, record.getTransportationMode());
				i++;
				if(previousInstant != null) {
					samplingStats.addValue(previousInstant.until(instant, ChronoUnit.SECONDS) / 60.0);
				}
				if(previousPoint != null) {
					pointsStats.addValue(Semantic.SPATIAL_LATLON.distance(previousPoint, point));
				}
				previousInstant = instant;
				previousPoint = point;
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
		List<SemanticTrajectory> read = new GeolifeTransportationModeDataReader().read();
		Multimap<String, SemanticTrajectory> classes = MultimapBuilder.hashKeys().arrayListValues().build();
		for (SemanticTrajectory t : read) {
			classes.put(MODE.getData(t, 0), t);
		}
		read = null;
		Set<String> keys = classes.keySet();
		for (String k : keys) {
			Collection<SemanticTrajectory> trajs = classes.get(k);
			System.out.printf("\tMODE '%s' - %d trajectories\n", k, trajs.size());
		}
	}
}
