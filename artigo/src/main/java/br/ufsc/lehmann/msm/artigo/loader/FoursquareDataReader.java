package br.ufsc.lehmann.msm.artigo.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
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

public class FoursquareDataReader implements IDataReader {
	
	private static int semantics_count = 3;
	
	public static final BasicSemantic<Integer> TIME = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Integer> LABEL = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<String> WEATHER = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<String> POI = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<String> DAY_OF_WEEK = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> PRICE = new BasicSemantic<>(semantics_count++);
	public static final BasicSemantic<Double> RATING = new BasicSemantic<>(semantics_count++);

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
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource("./datasets/foursquare_data.zip").getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("foursquare_data.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				//poi	price	rating	weather	label
				CSVFormat.EXCEL.withHeader("checkin_id", "tid", "lat_lon", "date_time", "time", "day", 
						"poi","price","rating","weather", "label").withDelimiter(','));
		Iterator<CSVRecord> pointsData = pointsParser.iterator();
		Multimap<Integer, FoursquareRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(data.getRecordNumber() == 1) {
				//skip header
				continue;
			}
			String lat_lon = data.get("lat_lon");
			String[] latLon = lat_lon.split(" ");
			int tid = Integer.parseInt(data.get("tid"));
			FoursquareRecord record = new FoursquareRecord(
					Integer.parseInt(data.get("checkin_id")),
				tid,
				new TPoint(Double.parseDouble(latLon[0]), Double.parseDouble(latLon[1])),
				Integer.parseInt(data.get("label")),
				StringUtils.defaultIfBlank(data.get("day"), null),
				Integer.parseInt(data.get("time")),
				StringUtils.defaultIfBlank(data.get("poi"), null),
				Double.parseDouble(data.get("price")),
				Double.parseDouble(data.get("rating")),
				StringUtils.defaultIfBlank(data.get("weather"), null)
			);
			records.put(record.getTid(), record);
		}
		zipFile.close();
		System.out.printf("Loaded %d points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantics_count);
			Collection<FoursquareRecord> collection = records.get(trajId);
			int i = 0;
			for (FoursquareRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				Timestamp t = new Timestamp(0);
				t.setMinutes(record.getTime());
				s.addData(i, Semantic.SPATIAL_LATLON, new TPoint(record.getLatlon().getX(), record.getLatlon().getY(), t));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(t.getTime()), Instant.ofEpochMilli(t.getTime())));
				s.addData(i, LABEL, record.getLabel());
				s.addData(i, TIME, record.getTime());
				s.addData(i, DAY_OF_WEEK, record.getDay());
				s.addData(i, POI, record.getPoi());
				s.addData(i, PRICE, record.getPrice());
				s.addData(i, RATING, record.getRating());
				s.addData(i, WEATHER, record.getWeather());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	public static void main(String[] args) {
		new FoursquareDataReader().read();
	}
}
