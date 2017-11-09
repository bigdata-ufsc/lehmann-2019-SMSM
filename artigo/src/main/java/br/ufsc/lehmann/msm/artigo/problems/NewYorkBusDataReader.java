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

public class NewYorkBusDataReader {
	
	private static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();

	public static final BasicSemantic<Double> DISTANCE = new BasicSemantic<>(3);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DIRECTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PHASE = new BasicSemantic<>(7);
	public static final BasicSemantic<Double> NEXT_STOP_DISTANCE = new BasicSemantic<>(8);
	public static final BasicSemantic<String> NEXT_STOP_ID = new BasicSemantic<>(9);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(10, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(10, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_SEMANTIC = new StopSemantic(10, new AttributeDescriptor<Stop, String>(AttributeType.STOP_TRAFFIC_LIGHT, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC = new StopSemantic(10, new AttributeDescriptor<Stop, Double>(AttributeType.STOP_TRAFFIC_LIGHT_DISTANCE, new NumberDistance()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION, 10)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));
	private boolean onlyStops;

	public NewYorkBusDataReader(boolean onlyStops) {
		this.onlyStops = onlyStops;
	}

	public List<SemanticTrajectory> read(String[] lines) throws IOException, ParseException {
		System.out.println("Reading file...");
		String dataFile = "./datasets/nyc.data.zip";
		if(!ArrayUtils.isEmpty(lines) && (ArrayUtils.contains(lines, "MTABC_BM3") && ArrayUtils.contains(lines, "MTABC_BM2"))) {//
			dataFile = "./datasets/nyc_bus_BM2-BM3.zip";
		} else if(!ArrayUtils.isEmpty(lines) && (ArrayUtils.contains(lines, "MTABC_Q52") && ArrayUtils.contains(lines, "MTABC_Q53"))) {
			dataFile = "./datasets/nyc_bus_Q52-Q53.zip";
		} else if(!ArrayUtils.isEmpty(lines) && (ArrayUtils.contains(lines, "MTA NYCT_S79+") && ArrayUtils.contains(lines, "MTA NYCT_S59") && ArrayUtils.contains(lines, "MTA NYCT_X1"))) {
			dataFile = "./datasets/nyc_bus_S59_S79_X1.zip";
		}
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(dataFile).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("bus.nyc_20140927.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "time", "vehicle_id", "route", "trip_id", "longitude", "latitude", "distance_along_trip", "infered_direction_id", "phase", "next_scheduled_stop_distance", "next_scheduled_stop_id", "semantic_stop_id", "semantic_move_id").withDelimiter(';'));
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street").withDelimiter(';'));
		InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("nyc.stop.traffic_light.csv")));
		CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
		
		InputStreamReader rawMovesEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_move.csv")));
		CSVParser movesParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawMovesEntry).toString(), 
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length").withDelimiter(';'));
		
		Map<Integer, Stop> stops = readStops(stopsParser, trafficLightsParser);
		Map<Integer, Move> moves = StopMoveCSVReader.moveCsvRead(movesParser, stops);
		List<SemanticTrajectory> ret = null;
		List<Move> usedMoves = new ArrayList<Move>();
		if(onlyStops) {
			ret = readStopsTrajectories(lines, pointsParser, stops, moves, usedMoves);
		} else {
			ret = readRawPoints(lines, pointsParser, stops, moves);
		}
		compute(usedMoves);
		zipFile.close();
		return ret;
	}

	public List<Stop> exportStops() throws IOException, ParseException, URISyntaxException {
		System.out.println("Reading file...");
		ZipFile zipFile = new ZipFile(new URI(this.getClass().getClassLoader().getResource("./datasets/nyc.data.zip").toString()).getPath());
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street").withDelimiter(';'));
		InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("nyc.stop.traffic_light.csv")));
		CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
		
		Map<Integer, Stop> stops = readStops(stopsParser, trafficLightsParser);
		zipFile.close();
		return new ArrayList<>(stops.values());
	}

	private Map<Integer, Stop> readStops(CSVParser stopsParser, CSVParser trafficLightsParser) throws IOException, ParseException {
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
		System.out.println("Fetching...");
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			NewYorkBusRecord record = new NewYorkBusRecord(
				Integer.parseInt(data.get("gid")),
				new Timestamp(DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP_US, StopMoveCSVReader.TIMESTAMP_BR).getTime()),
				Integer.parseInt(data.get("vehicle_id")),
				data.get("route"),
				data.get("trip_id"),
				Double.parseDouble(data.get("longitude")),
				Double.parseDouble(data.get("latitude")),
				Double.parseDouble(data.get("distance_along_trip")),
				Integer.parseInt(data.get("infered_direction_id")),
				data.get("phase"),
				Double.parseDouble(data.get("next_scheduled_stop_distance")),
				data.get("next_scheduled_stop_id"),
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
				StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getTripId(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 12);
			Collection<NewYorkBusRecord> collection = records.get(trajId);
			int i = 0;
			for (NewYorkBusRecord record : collection) {
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.remove(record.getSemanticStop());
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
							stops.put(record.getSemanticStop(), stop);
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
				s.addData(i, Semantic.GEOGRAPHIC, point);
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, ROUTE, record.getRoute());
				s.addData(i, DISTANCE, record.getDistanceAlongTrip());
				s.addData(i, VEHICLE, record.getVehicleId());
				s.addData(i, NEXT_STOP_DISTANCE, record.getNextStopDistance());
				s.addData(i, NEXT_STOP_ID, record.getNextStopId());
				s.addData(i, PHASE, record.getPhase());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(String[] lines, CSVParser pointsParser, Map<Integer, Stop> stops,
			Map<Integer, Move> moves) throws IOException, NumberFormatException, ParseException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		System.out.println("Fetching...");
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			NewYorkBusRecord record = new NewYorkBusRecord(
				Integer.parseInt(data.get("gid")),
				new Timestamp(DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP).getTime()),
				Integer.parseInt(data.get("vehicle_id")),
				data.get("route"),
				data.get("trip_id"),
				Double.parseDouble(data.get("longitude")),
				Double.parseDouble(data.get("latitude")),
				Double.parseDouble(data.get("distance_along_trip")),
				Integer.parseInt(data.get("infered_direction_id")),
				data.get("phase"),
				Double.parseDouble(data.get("next_scheduled_stop_distance")),
				data.get("next_scheduled_stop_id"),
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
				StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getTripId(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 12);
			Collection<NewYorkBusRecord> collection = records.get(trajId);
			int i = 0;
			for (NewYorkBusRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.GEOGRAPHIC, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, ROUTE, record.getRoute());
				s.addData(i, DISTANCE, record.getDistanceAlongTrip());
				s.addData(i, VEHICLE, record.getVehicleId());
				s.addData(i, NEXT_STOP_DISTANCE, record.getNextStopDistance());
				s.addData(i, NEXT_STOP_ID, record.getNextStopId());
				s.addData(i, PHASE, record.getPhase());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
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
