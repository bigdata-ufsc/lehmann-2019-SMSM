package br.ufsc.lehmann.msm.artigo.problems;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.ProportionalDistance;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveCSVReader.StopReaderCallback;
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;
import cc.mallet.util.IoUtils;

public class GeolifeUniversityDataReader {

	private static final SpatialDistanceFunction GEO_DISTANCE_FUNCTION = new EuclideanDistanceFunction();

	private static final SpatialDistanceFunction DISTANCE_FUNCTION = GEO_DISTANCE_FUNCTION;
	
	private static int SEMANTICS_COUNTER = 3;
	
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> TRANSPORTATION_MODE = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> REGION_INTEREST = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> PATH = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> DIRECTION = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, GEO_DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER++, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new ProportionalDistance(Thresholds.SLACK_TEMPORAL)));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(GEO_DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER++, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(GEO_DISTANCE_FUNCTION)));
	
	public static final BasicSemantic<String> PATH_WITH_DIRECTION = new BasicSemantic<String>(SEMANTICS_COUNTER++) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return DIRECTION.getData(p, i) + "/" + PATH.getData(p, i);
		}
	};
	
	private boolean onlyStops;
	private StopMoveStrategy strategy;

	public GeolifeUniversityDataReader(boolean onlyStops) {
		this(onlyStops, StopMoveStrategy.SMoT);
	}

	public GeolifeUniversityDataReader(boolean onlyStops, StopMoveStrategy strategy) {
		this.onlyStops = onlyStops;
		this.strategy = strategy;
	}

	public List<SemanticTrajectory> read(Integer... users) throws IOException, NumberFormatException, ParseException  {
		System.out.println("Reading file...");
		String filename = "./datasets/geolife_with_pois_university." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(filename).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("geolife.geolife_with_pois_university.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "tid", "lat", "lon", "time", "geom", "folder_id", "cid", "tid2", "POI", "semantic_stop_id", "semantic_move_id","path", "direction").withDelimiter(';'));
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.geolife_with_pois_university_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street", "POI").withDelimiter(';'));
		
		InputStreamReader rawMovesEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.geolife_with_pois_university_move.csv")));
		CSVParser movesParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawMovesEntry).toString(), 
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length").withDelimiter(';'));
		
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser, new StopReaderCallback() {
			
			@Override
			public void readFields(Stop stop, CSVRecord data) {
				stop.setStopName(data.get("POI"));
			}
		}, StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S");
		Map<Integer, Move> moves = StopMoveCSVReader.moveCsvRead(movesParser, stops, StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S");
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		System.out.println("Fetching...");
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(!ArrayUtils.isEmpty(users) && !ArrayUtils.contains(users, Integer.parseInt(data.get("folder_id")))) {
				continue;
			}
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			GeolifeRecord record = new GeolifeRecord(
					Integer.parseInt(data.get("tid")),
					Integer.parseInt(data.get("gid")),
					new Timestamp(
							DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss").getTime()),
					Double.parseDouble(data.get("lon")),
					Double.parseDouble(data.get("lat")),
					Integer.parseInt(data.get("folder_id")),
				null,
				data.get("POI"),
				data.get("path"),
				data.get("direction"),
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
						StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getTid(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());

		List<Move> allMoves = new ArrayList<>(moves.values());
		List<SemanticTrajectory> ret = null;
		if(onlyStops) {
			ret = readStopsTrajectories(stops, moves, records);
		} else {
			ret = readRawPoints(stops, moves, records);
		}
		compute(CollectionUtils.removeAll(allMoves, moves.values()));
		zipFile.close();
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Map<Integer, Stop> stops, Map<Integer, Move> moves, Multimap<Integer, GeolifeRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, SEMANTICS_COUNTER);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					if(stop == null) {
						throw new RuntimeException("Stop does not found");
					}
					stop.addPoint(point);
					if(i > 0) {
						if(STOP_CENTROID_SEMANTIC.getData(s, i - 1) == stop) {
							continue;
						}
						Stop previousStop = STOP_CENTROID_SEMANTIC.getData(s, i - 1);
						if(previousStop != null && previousStop.getNextMove() == null) {
							Move move = new Move(-1, previousStop, stop, previousStop.getEndTime(), stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									Angle.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
							previousStop.setNextMove(move);
							stop.setPreviousMove(move);
						}
					}
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
					s.addData(i, Semantic.GID, record.getGid());
					s.addData(i, Semantic.SPATIAL_LATLON, stop.getCentroid());
					s.addData(i, USER_ID, record.getUserId());
					s.addData(i, REGION_INTEREST, record.getPOI());
					s.addData(i, PATH, record.getPath());
					s.addData(i, DIRECTION, record.getDirection());
					i++;
				} else if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					if(move == null) {
						throw new RuntimeException("Move does not found");
					}
					move.getStart().setNextMove(move);
					move.getEnd().setPreviousMove(move);
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
				}
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(Map<Integer, Stop> stops, Map<Integer, Move> moves, Multimap<Integer, GeolifeRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, SEMANTICS_COUNTER);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.SPATIAL_LATLON, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, USER_ID, record.getUserId());
				s.addData(i, REGION_INTEREST, record.getPOI());
				s.addData(i, PATH, record.getPath());
				s.addData(i, DIRECTION, record.getDirection());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
				}
				if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, MOVE_ANGLE_SEMANTIC, move);
				}
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private void compute(Collection<Move> moves) {
		for (Move move : moves) {
			List<TPoint> points = new ArrayList<>();
			if(move.getStart() != null) {
				points.add(move.getStart().getEndPoint());
			}
			if(move.getPoints() != null) {
				points.addAll(Arrays.asList(move.getPoints()));
			}
			if(move.getEnd() != null) {
				points.add(move.getEnd().getStartPoint());
			}
			move.setAttribute(AttributeType.MOVE_ANGLE, Angle.getAngle(points.get(0), points.get(points.size() - 1)));
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, Distance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION));
		}
	}
}
