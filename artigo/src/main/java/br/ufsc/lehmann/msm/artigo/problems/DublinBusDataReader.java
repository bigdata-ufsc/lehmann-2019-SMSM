package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.core.trajectory.semantic.StopMove;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.msm.artigo.StopMoveSemantic;
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.LatLongDistanceFunction;
import cc.mallet.util.IoUtils;

public class DublinBusDataReader {
	public static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();
	
	public static final BasicSemantic<Integer> LINE_INFO = new BasicSemantic<>(3);
	public static final BasicSemantic<String> JOURNEY = new BasicSemantic<>(4);
	public static final BasicSemantic<Boolean> CONGESTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<Integer> STOP = new BasicSemantic<>(7);
	public static final BasicSemantic<String> OPERATOR = new BasicSemantic<>(8);
	
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(9, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(9, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_SEMANTIC = new StopSemantic(9, new AttributeDescriptor<Stop, String>(AttributeType.STOP_TRAFFIC_LIGHT, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC = new StopSemantic(9, new AttributeDescriptor<Stop, Double>(AttributeType.STOP_TRAFFIC_LIGHT_DISTANCE, new NumberDistance()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(10, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(10, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(10, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(10, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(10, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));

	private boolean onlyStops;

	public DublinBusDataReader(boolean onlyStops) {
		this.onlyStops = onlyStops;
	}

	public List<SemanticTrajectory> read(String[] lines) throws ZipException, IOException, NumberFormatException, ParseException {
		System.out.println("Reading file...");
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource("./datasets/dublin.data.zip").getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("bus.dublin_201301.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "time", "line_id", "journey_pattern", "vehicle_journey", "operator", "congestion", "longitude", "latitude", "block_journey_id", "vehicle_id", "stop_id", "semantic_stop_id", "semantic_move_id").withDelimiter(';'));
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_dublin_201301_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "begin", "end_lat", "end_lon", "length", "centroid_lat", "centroid_lon", "start_time", "end_time", "street").withDelimiter(';'));
		InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("dublin.stop.traffic_light.csv")));
		CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
		
		InputStreamReader rawMovesEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_dublin_201301_move.csv")));
		CSVParser movesParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawMovesEntry).toString(), 
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length", "angle", "traveled_distance").withDelimiter(';'));

		Map<Integer, Stop> stops = readStops(stopsParser, trafficLightsParser);
		Map<Integer, Move> moves = readMoves(movesParser, stops);

		List<Move> usedMoves = new ArrayList<Move>();
		List<SemanticTrajectory> ret = null;
		if(onlyStops) {
			ret = readStopsTrajectories(lines, pointsParser, stops, moves, usedMoves);
		} else {
			ret = loadRawPoints(lines, pointsParser, stops, moves);
		}
		compute(usedMoves);
		zipFile.close();
		return ret;
	}
	
	public List<Stop> exportStops() throws IOException, ParseException, URISyntaxException {
		System.out.println("Reading file...");
		ZipFile zipFile = new ZipFile(new URI(this.getClass().getClassLoader().getResource("./datasets/dublin.data.zip").toString()).getPath());
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_dublin_201301_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "begin", "end_lat", "end_lon", "length", "centroid_lat", "centroid_lon", "start_time", "end_time", "street").withDelimiter(';'));
		InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("dublin.stop.traffic_light.csv")));
		CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
		Map<Integer, Stop> stops = readStops(stopsParser, trafficLightsParser);
		zipFile.close();
		return new ArrayList<>(stops.values());
	}

	protected Map<Integer, Move> readMoves(CSVParser movesParser, Map<Integer, Stop> stops)
			throws IOException, ParseException {
		return StopMoveCSVReader.moveCsvRead(movesParser, stops);
	}

	protected Map<Integer, Stop> readStops(CSVParser stopsParser, CSVParser trafficLightsParser) throws IOException, ParseException {
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser);
		List<CSVRecord> records = trafficLightsParser.getRecords();
		Iterator<CSVRecord> trafficLightData = records.subList(1, records.size()).iterator();
		while(trafficLightData.hasNext()) {
			CSVRecord record = trafficLightData.next();
			Integer stopId = Integer.parseInt(record.get("stopId"));
			Stop stop = stops.get(stopId);
			if(stop == null) {
				throw new IllegalArgumentException("Unexistent stop mapped as " + stopId);
			}
			Long trafficLightId = Long.parseLong(record.get("trafficLightId"));
			stop.setTrafficLight(trafficLightId);
			Double trafficLightDistance = Double.parseDouble(record.get("trafficLightDistance"));
			stop.setTrafficLightDistance(trafficLightDistance);
		}
		return stops;
	}

	private List<SemanticTrajectory> readStopsTrajectories(String[] lines, CSVParser pointsParser, Map<Integer, Stop> stops, Map<Integer, Move> moves, List<Move> usedMoves) throws NumberFormatException, ParseException, IOException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		Multimap<Integer, DublinBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			String line = data.get("journey_pattern");
			if(!ArrayUtils.isEmpty(lines) && !ArrayUtils.contains(lines, line)) {
				continue;
			}
			DublinBusRecord record = new DublinBusRecord(
				Integer.parseInt(data.get("gid")),
				new Timestamp(DateUtils.parseDate(data.get("time"), "yyyy-MM-dd HH:mm:ssX").getTime()),
				Integer.parseInt(data.get("line_id")),
				line,
				Integer.parseInt(data.get("vehicle_journey")),
				data.get("operator"),
				"1".equals(data.get("congestion")),
				Double.parseDouble(data.get("longitude")),
				Double.parseDouble(data.get("latitude")),
				Integer.parseInt(data.get("block_journey_id")),
				Integer.parseInt(data.get("vehicle_id")),
				StringUtils.isEmpty(data.get("stop_id")) ? 0 : Integer.parseInt(data.get("stop_id")),
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
				StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getVehicle_journey(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<DublinBusRecord> collection = records.get(trajId);
			int i = 0;
			for (DublinBusRecord record : collection) {
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				if(record.getSemanticStopId() != null) {
					Stop stop = stops.remove(record.getSemanticStopId());
					if(stop == null) {
						continue;
					}
					if(i > 0) {
						Stop previousStop = STOP_CENTROID_SEMANTIC.getData(s, i - 1);
						if(previousStop != null) {
							Move move = new Move(-1, previousStop, stop, previousStop.getEndTime(), stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									Angle.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
							s.addData(i, MOVE_ANGLE_SEMANTIC, move);
							s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(move.getStartTime()), Instant.ofEpochMilli(move.getEndTime())));
							//injecting a move between two consecutives stops
							stops.put(record.getSemanticStopId(), stop);
						} else {
							s.addData(i, STOP_CENTROID_SEMANTIC, stop);
							s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
						}
					} else {
						s.addData(i, STOP_CENTROID_SEMANTIC, stop);
						s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
					}
				} else if(record.getSemanticMoveId() != null) {
					Move move = moves.remove(record.getSemanticMoveId());
					if(move == null) {
						for (int j = i - 1; j > -1; j--) {
							move = MOVE_ANGLE_SEMANTIC.getData(s, j);
							if(move != null) {
								break;
							}
						}
						if(move != null) {
							TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
							List<TPoint> a = new ArrayList<TPoint>(Arrays.asList(points));
							a.add(point);
							move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
							continue;
						}
					} else {
						usedMoves.add(move);
					}
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, MOVE_ANGLE_SEMANTIC, move);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(move.getStartTime()), Instant.ofEpochMilli(move.getEndTime())));
				}
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, LINE_INFO, record.getLineId());
				s.addData(i, JOURNEY, record.getJourney_pattern());
				s.addData(i, CONGESTION, record.isCongestion());
				s.addData(i, VEHICLE, record.getVehicle_id());
				s.addData(i, STOP, record.getStop_id());
				s.addData(i, OPERATOR, record.getOperator());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> loadRawPoints(String[] lines, CSVParser pointsParser, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws NumberFormatException, ParseException {
		Iterator<CSVRecord> pointsData = pointsParser.iterator();
		Multimap<Integer, DublinBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			String line = data.get("journey_pattern");
			if(!ArrayUtils.isEmpty(lines) && !ArrayUtils.contains(lines, line)) {
				continue;
			}
			DublinBusRecord record = new DublinBusRecord(
				Integer.parseInt(data.get("gid")),
				new Timestamp(DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP).getTime()),
				Integer.parseInt(data.get("line_id")),
				line,
				Integer.parseInt(data.get("vehicle_journey")),
				data.get("operator"),
				"1".equals(data.get("congestion")),
				Double.parseDouble(data.get("longitude")),
				Double.parseDouble(data.get("latitude")),
				Integer.parseInt(data.get("block_journey_id")),
				Integer.parseInt(data.get("vehicle_id")),
				StringUtils.isEmpty(data.get("stop_id")) ? 0 : Integer.parseInt(data.get("stop_id")),
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
				StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getVehicle_journey(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<DublinBusRecord> collection = records.get(trajId);
			int i = 0;
			for (DublinBusRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, LINE_INFO, record.getLineId());
				s.addData(i, JOURNEY, record.getJourney_pattern());
				s.addData(i, CONGESTION, record.isCongestion());
				s.addData(i, VEHICLE, record.getVehicle_id());
				s.addData(i, STOP, record.getStop_id());
				s.addData(i, OPERATOR, record.getOperator());
				if(record.getSemanticStopId() != null) {
					Stop stop = stops.get(record.getSemanticStopId());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
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
