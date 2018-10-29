package br.ufsc.lehmann.msm.artigo.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
		System.out.printf("Loaded %d points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantics_count);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL_LATLON, new TPoint(record.getLongitude(), record.getLatitude(), record.getTime()));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, USER_ID, record.getUserId());
				s.addData(i, MODE, record.getTransportationMode());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
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
