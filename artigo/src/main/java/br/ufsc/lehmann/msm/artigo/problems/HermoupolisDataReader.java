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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import br.ufsc.core.trajectory.SpatialDistanceFunction;
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
import br.ufsc.utils.EuclideanDistanceFunction;
import cc.mallet.util.IoUtils;

public class HermoupolisDataReader {
	
	public static final SpatialDistanceFunction DISTANCE_FUNCTION = new EuclideanDistanceFunction();
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(3);
	public static final BasicSemantic<String> GOAL = new BasicSemantic<>(4);
	public static final BasicSemantic<String> TRANSPORTATION = new BasicSemantic<>(5);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(6, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(6, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_NAME_SEMANTIC = new StopSemantic(6, new AttributeDescriptor<Stop, String>(AttributeType.STOP_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(7, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_TRANSPORTATION_MODE = new MoveSemantic(7, new AttributeDescriptor<Move, String>(AttributeType.MOVE_TRANSPORTATION_MODE, new EqualsDistanceFunction<String>()));
	
	private boolean onlyStops;
	private StopMoveStrategy strategy;

	public HermoupolisDataReader(boolean onlyStops) {
		this(onlyStops, StopMoveStrategy.SMoT);
	}

	public HermoupolisDataReader(boolean onlyStops, StopMoveStrategy strategy) {
		this.onlyStops = onlyStops;
		this.strategy = strategy;
	}

	public List<SemanticTrajectory> read(Integer... users) throws IOException, NumberFormatException, ParseException  {
		System.out.println("Reading file...");
		String filename = "./datasets/hermoupolis." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(filename).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("Scenario 1 - 1 profile-various moves.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("scenarioID", "MOid", "MPid", "edgeID", "realX", "realY", "relativeTime", "episodesems", "realTime", "semantic", "mode", "activity").withDelimiter(';'));
		
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		csvRecords = csvRecords.subList(1, csvRecords.size());

		Map<String, Stop> stops = readStops(csvRecords);
		Map<String, Move> moves = readMoves(csvRecords, stops);
		Iterator<CSVRecord> pointsData = csvRecords.iterator();
		System.out.println("Fetching...");
		Multimap<String, HermoupolisRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(!ArrayUtils.isEmpty(users) && !ArrayUtils.contains(users, Integer.parseInt(data.get("user_id")))) {
				continue;
			}
			String episode = data.get("episodesems");
			String semantic = data.get("semantic");
			HermoupolisRecord record = new HermoupolisRecord(
				new Timestamp(
						DateUtils.parseDate(data.get("realTime"), StopMoveCSVReader.TIMESTAMP, "mm:ss.S").getTime()),
						Integer.parseInt(data.get("scenarioID")),
						Integer.parseInt(data.get("MOid")),
						Integer.parseInt(data.get("MPid")),
						Integer.parseInt(data.get("edgeID")),
						Double.parseDouble(data.get("relativeTime")),
						Double.parseDouble(data.get("realX")),
						Double.parseDouble(data.get("realY")),
						data.get("mode"),
						data.get("activity"),
						StringUtils.equals("STOP", semantic) ? Integer.parseInt(episode) : null,
						StringUtils.equals("MOVE", semantic) ? Integer.parseInt(episode) : null
			);
			records.put(record.getScenario() + "_" + record.getMoid(), record);
		}
		System.out.printf("Loaded %d GPS points from dataset\n", records.size());
		System.out.printf("Loaded %d trajectories from dataset\n", records.keySet().size());

		List<SemanticTrajectory> ret = null;
		if(onlyStops) {
			ret = readStopsTrajectories(stops, moves, records);
		} else {
			ret = readRawPoints(stops, moves, records);
		}
		
		zipFile.close();
		return ret;
	}

	private Map<String, Move> readMoves(List<CSVRecord> csvRecords, Map<String, Stop> stops) throws ParseException {
		Map<String, Move> ret = new HashMap<>();

		Multimap<String, CSVRecord> episodes = MultimapBuilder.hashKeys().arrayListValues().build();
		for (CSVRecord record : csvRecords) {
			episodes.put(record.get("scenarioID") + "_" + record.get("MOid"), record);
		}
		Map<String, Collection<CSVRecord>> episodeMaps = episodes.asMap();
		for (Map.Entry<String, Collection<CSVRecord>> epi : episodeMaps.entrySet()) {
			List<CSVRecord> epiList = new ArrayList<>(epi.getValue());
			Collections.sort(epiList, Comparator.comparingDouble(r -> Double.parseDouble(r.get("relativeTime"))));
			CSVRecord previousStop = null;
			List<CSVRecord> previousMove = new ArrayList<>();
			int startMoveIndex = 0;
			int pointIndex = 0;
			for (CSVRecord record : epiList) {
				if("MOVE".equals(record.get("semantic"))) {
					if(previousMove.isEmpty()) {
						startMoveIndex = pointIndex;
					}
					previousMove.add(record);
				} else {
					if(!previousMove.isEmpty()) {
						CSVRecord initialMove = previousMove.get(0);
						CSVRecord endMove = previousMove.get(previousMove.size() - 1);
						String moveId = initialMove.get("scenarioID") + "_" + initialMove.get("MOid") + "_" +initialMove.get("episodesems");
						Stop initialStop = stops.get(previousStop.get("scenarioID") + "_" + previousStop.get("MOid") + "_" +previousStop.get("episodesems"));
						Stop endStop = stops.get(record.get("scenarioID") + "_" + record.get("MOid") + "_" +record.get("episodesems"));
						long initialTime = (long) (Double.parseDouble(initialMove.get("relativeTime")) * 1000);
						long endTime = (long) (Double.parseDouble(endMove.get("relativeTime")) * 1000);
						List<TPoint> points = previousMove.stream().map(rec -> new TPoint(Double.parseDouble(rec.get("realX")), Double.parseDouble(rec.get("realY")))).collect(Collectors.toList());
						Move move = new Move(moveId.hashCode(), initialStop, endStop, initialTime, endTime, startMoveIndex, previousMove.size(), points.toArray(new TPoint[points.size()]));
						move.setAttribute(AttributeType.MOVE_TRANSPORTATION_MODE, initialMove.get("mode"));
						move.setAttribute(AttributeType.MOVE_ACTIVITY, initialMove.get("activity"));
						ret.put(moveId, move);
					} else {
						previousStop = record;
					}
					previousMove.clear();
				}
				pointIndex++;
			}
		}
		return ret;
	}

	private Map<String, Stop> readStops(List<CSVRecord> csvRecords) throws ParseException {
		Map<String, Stop> ret = new HashMap<>();

		Multimap<String, CSVRecord> episodes = MultimapBuilder.hashKeys().arrayListValues().build();
		for (CSVRecord record : csvRecords) {
			episodes.put(record.get("scenarioID") + "_" + record.get("MOid"), record);
		}
		Map<String, Collection<CSVRecord>> episodeMaps = episodes.asMap();
		for (Map.Entry<String, Collection<CSVRecord>> epi : episodeMaps.entrySet()) {
			List<CSVRecord> epiList = new ArrayList<>(epi.getValue());
			Collections.sort(epiList, Comparator.comparingDouble(r -> Double.parseDouble(r.get("relativeTime"))));
			List<CSVRecord> currentStop = new ArrayList<>();
			int startStopIndex = 0;
			int pointIndex = 0;
			for (CSVRecord record : epiList) {
				if("STOP".equals(record.get("semantic"))) {
					if(currentStop.isEmpty()) {
						startStopIndex = pointIndex;
					}
					currentStop.add(record);
				} else {
					if(!currentStop.isEmpty()) {
						CSVRecord initialStop = currentStop.get(0);
						CSVRecord endStop = currentStop.get(currentStop.size() - 1);
						String stopId = record.get("scenarioID") + "_" + record.get("MOid") + "_" +record.get("episodesems");
						TPoint initialPoint = new TPoint(Double.parseDouble(initialStop.get("realX")), Double.parseDouble(initialStop.get("realY")));
						TPoint endPoint = new TPoint(Double.parseDouble(endStop.get("realX")), Double.parseDouble(endStop.get("realY")));
						TPoint p = new TPoint(initialPoint.getX() + endPoint.getX() / 2, initialPoint.getY() + endPoint.getY() / 2);
						long initialTime = (long) (Double.parseDouble(initialStop.get("relativeTime")) * 1000);
						long endTime = (long) (Double.parseDouble(endStop.get("relativeTime")) * 1000);
						ret.put(stopId, new Stop(stopId.hashCode(), endStop.get("activity"), initialTime, endTime, initialPoint, startStopIndex, endPoint, 2, p, null));
						currentStop.clear();
						startStopIndex = 0;
					}
					currentStop.clear();
					startStopIndex = 0;
				}
				if(!currentStop.isEmpty()) {
					CSVRecord initialStop = currentStop.get(0);
					CSVRecord endStop = currentStop.get(currentStop.size() - 1);
					String stopId = record.get("scenarioID") + "_" + record.get("MOid") + "_" +record.get("episodesems");
					TPoint initialPoint = new TPoint(Double.parseDouble(initialStop.get("realX")), Double.parseDouble(initialStop.get("realY")));
					TPoint endPoint = new TPoint(Double.parseDouble(endStop.get("realX")), Double.parseDouble(endStop.get("realY")));
					TPoint p = new TPoint(initialPoint.getX() + endPoint.getX() / 2, initialPoint.getY() + endPoint.getY() / 2);
					long initialTime = (long) (Double.parseDouble(initialStop.get("relativeTime")) * 1000);
					long endTime = (long) (Double.parseDouble(endStop.get("relativeTime")) * 1000);
					ret.put(stopId, new Stop(stopId.hashCode(), endStop.get("activity"), initialTime, endTime, initialPoint, startStopIndex, endPoint, 2, p, null));
					currentStop.clear();
					startStopIndex = 0;
				}
				pointIndex++;
			}
		}
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Map<String, Stop> stops, Map<String, Move> moves, Multimap<String, HermoupolisRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 8);
			Collection<HermoupolisRecord> collection = records.get(trajId);
			int i = 0;
			for (HermoupolisRecord record : collection) {
				TPoint point = new TPoint(record.getRealX(), record.getRealY());
				if(record.getStopId() != null) {
					Stop stop = stops.remove(record.getScenario() + "_" + record.getMoid() + "_" + record.getStopId());
					if(stop == null) {
						continue;
					}
					if(i > 0) {
						Stop previousStop = STOP_CENTROID_SEMANTIC.getData(s, i - 1);
						if(previousStop != null) {
							Move move = new Move(-1, previousStop, stop, previousStop.getEndTime(), stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									Angle.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
							s.addData(i, MOVE_TRANSPORTATION_MODE, move);
							s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(move.getStartTime()), Instant.ofEpochMilli(move.getEndTime())));
							//injecting a move between two consecutives stops
							stops.put(record.getScenario() + "_" + record.getMoid() + "_" +record.getStopId(), stop);
						} else {
							s.addData(i, STOP_CENTROID_SEMANTIC, stop);
							s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
						}
					} else {
						s.addData(i, STOP_CENTROID_SEMANTIC, stop);
						s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
					}
				} else if(record.getMoveId() != null) {
					Move move = moves.remove(record.getScenario() + "_" + record.getMoid() + "_" +record.getMoveId());
					if(move == null) {
						for (int j = i - 1; j > -1; j--) {
							move = MOVE_TRANSPORTATION_MODE.getData(s, j);
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
						} else {
							throw new RuntimeException("Move does not found");
						}
					}
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(move.getStartTime()), Instant.ofEpochMilli(move.getEndTime())));
					s.addData(i, MOVE_TRANSPORTATION_MODE, move);
				}
				s.addData(i, Semantic.GID, record.getEdgeId());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, USER_ID, record.getMoid());
				s.addData(i, GOAL, record.getActivity());
				s.addData(i, TRANSPORTATION, record.getTransportationMode());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(Map<String, Stop> stops, Map<String, Move> moves, Multimap<String, HermoupolisRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 8);
			Collection<HermoupolisRecord> collection = records.get(trajId);
			int i = 0;
			for (HermoupolisRecord record : collection) {
				TPoint point = new TPoint(record.getRealX(), record.getRealY());
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, Semantic.GID, record.getEdgeId());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, USER_ID, record.getMoid());
				s.addData(i, GOAL, record.getActivity());
				s.addData(i, TRANSPORTATION, record.getTransportationMode());
				if(record.getStopId() != null) {
					Stop stop = stops.get(record.getScenario() + "_" + record.getMoid() + "_" +record.getStopId());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
				}
				if(record.getMoveId() != null) {
					Move move = moves.get(record.getScenario() + "_" + record.getMoid() + "_" +record.getMoveId());
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, MOVE_TRANSPORTATION_MODE, move);
				}
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}
