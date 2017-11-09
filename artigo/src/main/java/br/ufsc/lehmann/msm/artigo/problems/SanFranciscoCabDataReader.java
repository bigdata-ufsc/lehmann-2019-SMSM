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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
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

public class SanFranciscoCabDataReader {
	
	private static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();
	
	public static final BasicSemantic<Integer> TID = new BasicSemantic<>(3);
	public static final BasicSemantic<Integer> OCUPATION = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> ROAD = new BasicSemantic<>(5);
	public static final BasicSemantic<String> DIRECTION = new BasicSemantic<>(6);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_TRAFFIC_LIGHT, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, Double>(AttributeType.STOP_TRAFFIC_LIGHT_DISTANCE, new NumberDistance()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION, 10)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));

	public static final BasicSemantic<String> REGION_INTEREST = new BasicSemantic<>(9);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(10);
	public static final BasicSemantic<String> ROUTE_WITH_DIRECTION = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return DIRECTION.getData(p, i) + "/" + ROUTE.getData(p, i);
		}
	};
	public static final BasicSemantic<String> ROUTE_IN_ROADS_WITH_DIRECTION = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return DIRECTION.getData(p, i) + "/" + ROAD.getData(p, i) + "/" + ROUTE.getData(p, i);
		}
	};
	public static final BasicSemantic<String> ROADS_WITH_DIRECTION = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return ROAD.getData(p, i) + "/" + ROUTE.getData(p, i);
		}
	};
	public static final BasicSemantic<String> DIRECTION_ROAD = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return DIRECTION.getData(p, i) + "/" + ROAD.getData(p, i);
		}
	};
	private String[] roads;
	private boolean onlyStops;

	private String[] directions;

	private String[] regions;

	private StopMoveStrategy strategy = StopMoveStrategy.CBSMoT;
	
	public SanFranciscoCabDataReader(boolean onlyStop) {
		this.onlyStops = onlyStop;
	}

	public SanFranciscoCabDataReader(boolean onlyStop, String[] roads) {
		this(onlyStop);
		this.roads = roads;
	}

	public SanFranciscoCabDataReader(boolean onlyStop, String[] roads, String[] directions) {
		this(onlyStop, roads);
		this.directions = directions;
	}
	
	public SanFranciscoCabDataReader(boolean onlyStop, StopMoveStrategy strategy, String[] roads, String[] directions, String[] regions) {
		this(onlyStop, roads, directions);
		this.strategy  = strategy;
		this.regions = regions;
	}

	public List<SemanticTrajectory> read() throws IOException, ParseException {
		System.out.println("Reading file...");
		String filename = "./datasets/sanfrancisco." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(filename).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("taxi.sanfrancisco_taxicab_crawdad.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "tid", "taxi_id", "lat", "lon", "timestamp", "ocupation", "airport", "mall", "road", "direction", "intersection_101_280", "bayshore_fwy", "stop", "semantic_stop_id", "semantic_move_id", "route").withDelimiter(';'));
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.taxi_sanfrancisco_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street").withDelimiter(';'));
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser);

		ZipEntry entry = zipFile.getEntry("sanfrancisco.stop.traffic_light.csv");
		if(entry != null) {
			InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(entry));
			CSVParser trafficLightsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawTrafficLightsEntry).toString(), 
					CSVFormat.EXCEL.withHeader("stopId", "trafficLightId", "trafficLightDistance").withDelimiter(';'));
			readTrafficLights(trafficLightsParser, stops);
		}

		InputStreamReader rawMovesEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.taxi_sanfrancisco_move.csv")));
		CSVParser movesParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawMovesEntry).toString(), 
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length").withDelimiter(';'));
		
		Map<Integer, Move> moves = StopMoveCSVReader.moveCsvRead(movesParser, stops);
		List<Move> allMoves = new ArrayList<>(moves.values());
		List<SemanticTrajectory> ret = null;
		if(onlyStops) {
			ret = readStopsTrajectories(pointsParser, stops, moves);
		} else {
			ret = loadRawPoints(pointsParser, stops, moves);
		}
		compute(CollectionUtils.removeAll(allMoves, moves.values()));
		zipFile.close();
		return ret;
	}
	
	public List<Stop> exportStops() throws IOException, ParseException, URISyntaxException {
		System.out.println("Reading file...");
		ZipFile zipFile = new ZipFile(new URI(this.getClass().getClassLoader().getResource("./datasets/sanfrancisco.data.zip").toString()).getPath());
		
		InputStreamReader rawStopsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("stops_moves.taxi_sanfrancisco_stop.csv")));
		CSVParser stopsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawStopsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street").withDelimiter(';'));
		Map<Integer, Stop> stops = StopMoveCSVReader.stopsCsvRead(stopsParser);

		ZipEntry entry = zipFile.getEntry("sanfrancisco.stop.traffic_light.csv");
		if(entry != null) {
			InputStreamReader rawTrafficLightsEntry = new InputStreamReader(zipFile.getInputStream(entry));
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

	private List<SemanticTrajectory> readStopsTrajectories(CSVParser pointsParser, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws NumberFormatException, ParseException, IOException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		Multimap<Integer, SanFranciscoCabRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			String road = data.get("road");
			String direction = data.get("direction");
			String region = data.get("stop");
			String route = data.get("route");
			if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.contains(roads, road)) {
				continue;
			}
			if(!ArrayUtils.isEmpty(directions) && !ArrayUtils.contains(directions, direction)) {
				continue;
			}
			SanFranciscoCabRecord record = new SanFranciscoCabRecord(
					Integer.parseInt(data.get("tid")),
					Integer.parseInt(data.get("gid")),
							Integer.parseInt(data.get("taxi_id")),
				new Timestamp(DateUtils.parseDate(data.get("timestamp"), StopMoveCSVReader.TIMESTAMP).getTime()),
				Integer.parseInt(data.get("ocupation")),
				Double.parseDouble(data.get("lon")),
				Double.parseDouble(data.get("lat")),
				true,
				true,
				Integer.parseInt(road),
				direction,
				region,
				route,
				StringUtils.isEmpty(stop) ? null : Integer.parseInt(stop),
				StringUtils.isEmpty(move) ? null : Integer.parseInt(move)
			);
			records.put(record.getTid(), record);
		}
		if(regions != null) {
			records = filterRecordsByRegions(records, regions);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<SanFranciscoCabRecord> collection = records.get(trajId);
			int i = 0;
			for (SanFranciscoCabRecord record : collection) {
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
							//injecting a move between two consecutives stops
							stops.put(record.getSemanticStop(), stop);
						} else {
							s.addData(i, STOP_CENTROID_SEMANTIC, stop);
						}
					} else {
						s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					}
				} else if(record.getSemanticMoveId() != null) {
					Move move = moves.remove(record.getSemanticMoveId());
					if(move == null) {
						for (int j = 0; j < i; j++) {
							move = MOVE_ANGLE_SEMANTIC.getData(s, j);
							if(move != null) {
								break;
							}
						}
						if(move != null) {
							TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
							List<TPoint> a = new ArrayList<TPoint>(Arrays.asList(points));
							a.add(point);
							points = a.toArray(new TPoint[a.size()]);
							move.setAttribute(AttributeType.MOVE_POINTS, points);
							continue;
						}
					}
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					points = a.toArray(new TPoint[a.size()]);
					move.setAttribute(AttributeType.MOVE_POINTS, points);
					s.addData(i, MOVE_ANGLE_SEMANTIC, move);
				}
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.GEOGRAPHIC, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, TID, record.getTid());
				s.addData(i, OCUPATION, record.getOcupation());
				s.addData(i, ROAD, record.getRoad());
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, REGION_INTEREST, record.getRegion());
				s.addData(i, ROUTE, record.getRoute());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Loaded %d trajectories (filtered)\n", ret.size());
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private static Multimap<Integer, SanFranciscoCabRecord> filterRecordsByRegions(
			Multimap<Integer, SanFranciscoCabRecord> records, String[] regions) {

		Set<Integer> keys = new HashSet<>(records.keySet());
		main: for (Integer trajId : keys) {
			Collection<SanFranciscoCabRecord> recs = records.get(trajId);
			ArrayList<String> untouchedRegions = new ArrayList<>(Arrays.asList(regions));
			for (SanFranciscoCabRecord rec : recs) {
				if(untouchedRegions.contains(rec.getRegion())) {
					untouchedRegions.remove(rec.getRegion());
				}
				if(untouchedRegions.isEmpty()) {
					continue main;
				}
			}
			records.removeAll(trajId);
		}
		return records;
	}

	private List<SemanticTrajectory> loadRawPoints(CSVParser pointsParser, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws NumberFormatException, ParseException, IOException {
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		Multimap<Integer, SanFranciscoCabRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			String road = data.get("road");
			String direction = data.get("direction");
			String region = data.get("stop");
			String route = data.get("route");
			if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.contains(roads, road)) {
				continue;
			}
			if(!ArrayUtils.isEmpty(directions) && !ArrayUtils.contains(directions, direction)) {
				continue;
			}
			SanFranciscoCabRecord record = new SanFranciscoCabRecord(
					Integer.parseInt(data.get("tid")),
					Integer.parseInt(data.get("gid")),
							Integer.parseInt(data.get("taxi_id")),
				new Timestamp(DateUtils.parseDate(data.get("time"), StopMoveCSVReader.TIMESTAMP).getTime()),
				Integer.parseInt(data.get("ocupation")),
				Double.parseDouble(data.get("lon")),
				Double.parseDouble(data.get("lat")),
				true,
				true,
				Integer.parseInt(road),
				direction,
				region,
				route,
				stop == null ? null : Integer.parseInt(stop),
				move == null ? null : Integer.parseInt(move)
			);
			records.put(record.getTid(), record);
		}
		if(regions != null) {
			records = filterRecordsByRegions(records, regions);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<SanFranciscoCabRecord> collection = records.get(trajId);
			int i = 0;
			for (SanFranciscoCabRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.GEOGRAPHIC, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, TID, record.getTid());
				s.addData(i, OCUPATION, record.getOcupation());
				s.addData(i, ROAD, record.getRoad());
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, REGION_INTEREST, record.getRegion());
				s.addData(i, ROUTE, record.getRoute());
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
		System.out.printf("Loaded %d trajectories (filtered)\n", ret.size());
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
