package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.beanutils.PropertyUtilsBean;
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
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.LatLongDistanceFunction;
import cc.mallet.util.IoUtils;

public class NewYorkBusDataReader {
	
	public static final LatLongDistanceFunction GEO_DISTANCE_FUNCTION = new LatLongDistanceFunction();

	public static final BasicSemantic<Double> DISTANCE = new BasicSemantic<>(3);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DIRECTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PHASE = new BasicSemantic<>(7);
	public static final BasicSemantic<Double> NEXT_STOP_DISTANCE = new BasicSemantic<>(8);
	public static final BasicSemantic<String> NEXT_STOP_ID = new BasicSemantic<>(9);
	public static final BasicSemantic<String> REGION_INTEREST = new BasicSemantic<>(10);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(11, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(11, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, GEO_DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(11, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(12, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(12, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(12, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(12, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(GEO_DISTANCE_FUNCTION, Thresholds.MOVE_INNERPOINTS_DISTANCE)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(12, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(GEO_DISTANCE_FUNCTION)));

	public static final BasicSemantic<String> ROUTE_WITH_DIRECTION = new BasicSemantic<String>(10) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return ROUTE.getData(p, i) + "/" + DIRECTION.getData(p, i);
		}
	};
	private boolean onlyStops;

	private StopMoveStrategy strategy;
	
	public NewYorkBusDataReader(boolean onlyStops) {
		this(onlyStops, StopMoveStrategy.CBSMoT);
	}

	public NewYorkBusDataReader(boolean onlyStops, StopMoveStrategy strategy) {
		this.onlyStops = onlyStops;
		this.strategy = strategy;
	}
	
	public List<SemanticTrajectory> read(String[] lines) throws IOException, ParseException {
		return read(new CSVRegisterFilter("route", lines));
	}

	public List<SemanticTrajectory> read(CSVRegisterFilter filter) throws IOException, ParseException {
		System.out.println("Reading file...");
		String dataFile = "./datasets/nyc." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(dataFile).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("bus.nyc_20140927.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "time", "vehicle_id", "route", "trip_id", "longitude", "latitude", "distance_along_trip", "infered_direction_id", "phase", "next_scheduled_stop_distance", "next_scheduled_stop_id", "POI", "semantic_stop_id", "semantic_move_id").withDelimiter(';'));
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street", "POI").withDelimiter(';'));
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser, new StopMoveCSVReader.StopReaderCallback() {
			
			@Override
			public void readFields(Stop stop, CSVRecord data) {
				stop.setRegion(data.get("POI"));
				stop.setStopName((String) stop.getRegion());
			}
		}, StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss.SSS");

		ZipEntry trafficLightEntry = zipFile.getEntry("nyc.stop.traffic_light.csv");
		if(trafficLightEntry != null) {
			InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(trafficLightEntry));
			CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
					CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
			readTrafficLights(trafficLightsParser, stops);
		}
		
		InputStreamReader rawMovesEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_move.csv")));
		CSVParser movesParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawMovesEntry).toString(), 
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length").withDelimiter(';'));
		
		Map<Integer, Move> moves = StopMoveCSVReader.moveCsvRead(movesParser, stops, StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S");
		List<SemanticTrajectory> ret = null;
		List<Move> usedMoves = new ArrayList<Move>();
		if(onlyStops) {
			ret = readStopsTrajectories(filter, pointsParser, stops, moves, usedMoves);
		} else {
			ret = readRawPoints(filter, pointsParser, stops, moves);
		}
		compute(usedMoves);
		zipFile.close();
		return ret;
	}

	public List<Stop> exportStops() throws IOException, ParseException, URISyntaxException {
		System.out.println("Reading file...");
		String dataFile = "./datasets/nyc." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(new URI(this.getClass().getClassLoader().getResource(dataFile).toString()).getPath());

		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.bus_nyc_20140927_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(),
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time",
						"end_time", "begin", "length", "street", "POI").withDelimiter(';'));
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser, new StopMoveCSVReader.StopReaderCallback() {

			@Override
			public void readFields(Stop stop, CSVRecord data) {
				stop.setRegion(data.get("POI"));
			}
		});

		ZipEntry trafficLightEntry = zipFile.getEntry("nyc.stop.traffic_light.csv");
		if (trafficLightEntry != null) {
			InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(trafficLightEntry));
			CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(),
					CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
			readTrafficLights(trafficLightsParser, stops);
		}
		zipFile.close();
		return new ArrayList<>(stops.values());
	}

	private void readTrafficLights(CSVParser trafficLightsParser, Map<Integer, Stop> stops) throws IOException {
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
	}

	private List<SemanticTrajectory> readStopsTrajectories(CSVRegisterFilter filter, CSVParser pointsParser, Map<Integer, Stop> stops, Map<Integer, Move> moves, List<Move> usedMoves) throws NumberFormatException, ParseException, IOException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		System.out.println("Fetching...");
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(filter != null && !filter.accept(data)) {
				continue;
			}
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			NewYorkBusRecord record = new NewYorkBusRecord(
				Integer.parseInt(data.get("gid")),
				new Timestamp(DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP, "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S").getTime()),
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
				data.get("POI"),
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, 13);
			Collection<NewYorkBusRecord> collection = records.get(trajId);
			if(!filter.accept(collection)) {
				continue;
			}
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
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, new LatLongDistanceFunction()));
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
				s.addData(i, Semantic.SPATIAL, point);
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

	private List<SemanticTrajectory> readRawPoints(CSVRegisterFilter filter, CSVParser pointsParser, Map<Integer, Stop> stops,
			Map<Integer, Move> moves) throws IOException, NumberFormatException, ParseException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		System.out.println("Fetching...");
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(filter != null && !filter.accept(data)) {
				continue;
			}
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
				data.get("POI"),
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, 13);
			Collection<NewYorkBusRecord> collection = records.get(trajId);
			if(!filter.accept(collection)) {
				continue;
			}
			int i = 0;
			for (NewYorkBusRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.SPATIAL, point);
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
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, Distance.getDistance(points.toArray(new TPoint[points.size()]), new LatLongDistanceFunction()));
		}
	}
	
	public static class CSVRegisterFilter {
		
		private String field;
		private String[] values;
		private boolean allValues;
		
		public CSVRegisterFilter(String field, String[] values) {
			this(field, values, true);
		}

		public CSVRegisterFilter(String field, String[] values, boolean allValues) {
			this.allValues = allValues;
			this.field = field;
			this.values = values;
			
		}

		public boolean accept(CSVRecord rec) {
			if(rec.isMapped(field)) {
				if(ArrayUtils.contains(values, rec.get(field))) {
					return true;
				}
			}
			return false;
		}

		public boolean accept(Collection<NewYorkBusRecord> recs) {
			List<String> untouchedValues = new ArrayList<>(Arrays.asList(values));
			try {
				PropertyUtilsBean bean = new PropertyUtilsBean();
				for (NewYorkBusRecord rec : recs) {
					if(!bean.isReadable(rec, field)) {
						return false;
					}
					if(allValues) {
						if(untouchedValues.contains(bean.getProperty(rec, field))) {
							untouchedValues.remove(bean.getProperty(rec, field));
						}
						if(untouchedValues.isEmpty()) {
							return true;
						}
					} else {
						if(untouchedValues.contains(bean.getProperty(rec, field))) {
							return true;
						}
					}
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			return false;
		}
	}
}
