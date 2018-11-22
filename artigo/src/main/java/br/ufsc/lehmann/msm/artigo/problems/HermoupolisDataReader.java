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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;
import cc.mallet.util.IoUtils;

public class HermoupolisDataReader implements IDataReader {
	
	private static int semantic_counter = 3;
	
	public static final SpatialDistanceFunction DISTANCE_FUNCTION = new EuclideanDistanceFunction();
	public static final BasicSemantic<Integer> MPID = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<Integer> MOID = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> MODE = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> ACTIVITY = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> SEMANTIC_TRAJECTORY_TAG = new BasicSemantic<>(semantic_counter++);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(semantic_counter, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(semantic_counter, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_NAME_SEMANTIC = new StopSemantic(semantic_counter++, new AttributeDescriptor<Stop, String>(AttributeType.STOP_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(semantic_counter, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_TRANSPORTATION_MODE = new MoveSemantic(semantic_counter++, new AttributeDescriptor<Move, String>(AttributeType.MOVE_TRANSPORTATION_MODE, new EqualsDistanceFunction<String>()));
	
	private boolean onlyStops;
	private StopMoveStrategy strategy;
	private String fileName;

	public HermoupolisDataReader(boolean onlyStops, String fileName) {
		this(onlyStops, StopMoveStrategy.SMoT, fileName);
	}

	public HermoupolisDataReader(boolean onlyStops) {
		this(onlyStops, StopMoveStrategy.SMoT);
	}

	public HermoupolisDataReader(boolean onlyStops, StopMoveStrategy strategy) {
		this(onlyStops, strategy, "Scenario 1 - 1 profile-various moves.csv");
	}

	public HermoupolisDataReader(boolean onlyStops, StopMoveStrategy strategy, String fileName) {
		this.onlyStops = onlyStops;
		this.strategy = strategy;
		this.fileName = fileName;
	}

	@Override
	public List<SemanticTrajectory> read() {
		try {
			return _read();
		} catch (NumberFormatException | IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public List<SemanticTrajectory> _read() throws IOException, NumberFormatException, ParseException  {
		System.out.println("Reading file...");
		String filename = "./datasets/hermoupolis." + strategy.name().toLowerCase() + ".data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(this.getClass().getClassLoader().getResource(filename).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(fileName)));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("scenarioID", "MOid", "MPid", "edgeID", "realX", "realY", "realTime", "relativeTime", "episodesems", "episId", "semantic", "mode", "activity", "sem_traj_tag").withDelimiter(';'));
		
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		csvRecords = csvRecords.subList(1, csvRecords.size());
		
		Map<String, Stop> stops = readStops(csvRecords);
		Map<String, Move> moves = readMoves(csvRecords, stops);
		Iterator<CSVRecord> pointsData = csvRecords.iterator();
		System.out.println("Fetching...");
		Multimap<String, HermoupolisRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		int gid = 0;
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			String episode = data.get("episId");
			String semantic = data.get("semantic");
			HermoupolisRecord record = new HermoupolisRecord(gid++,
				new Timestamp(DateUtils.parseDate(data.get("relativeTime"), StopMoveCSVReader.TIMESTAMP + ".S").getTime()),
						Integer.parseInt(data.get("scenarioID")),
						Integer.parseInt(data.get("MOid")),
						Integer.parseInt(data.get("MPid")),
						Integer.parseInt(data.get("edgeID")),
						Double.parseDouble(data.get("realTime")),
						Double.parseDouble(data.get("realX")),
						Double.parseDouble(data.get("realY")),
						data.get("mode"),
						data.get("activity"),
						data.get("sem_traj_tag"),
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
			Collections.sort(epiList, Comparator.comparingDouble(r -> Double.parseDouble(r.get("realTime"))));
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
						String scenarioID = initialMove.get("scenarioID");
						String moid = initialMove.get("MOid");
						String episID = initialMove.get("episId");
						String moveId = scenarioID + "_" + moid + "_" +episID;
						Stop initialStop = stops.get(previousStop.get("scenarioID") + "_" + previousStop.get("MOid") + "_" +previousStop.get("episId"));
						Stop endStop = stops.get(record.get("scenarioID") + "_" + record.get("MOid") + "_" +record.get("episId"));
						long initialTime = (long) (Double.parseDouble(initialMove.get("realTime")) * 1000);
						long endTime = (long) (Double.parseDouble(endMove.get("realTime")) * 1000);
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
		for (Map.Entry<String, Move> entry : ret.entrySet()) {
			Move move = entry.getValue();

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
			double distance = Distance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION);
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, distance);
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
			Collections.sort(epiList, Comparator.comparingDouble(r -> Double.parseDouble(r.get("realTime"))));
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
						CSVRecord lastRecord = currentStop.get(currentStop.size() - 1);
						CSVRecord initialStop = currentStop.get(0);
						CSVRecord endStop = currentStop.get(currentStop.size() - 1);
						String stopId = lastRecord.get("scenarioID") + "_" + lastRecord.get("MOid") + "_" +lastRecord.get("episId");
						TPoint initialPoint = new TPoint(Double.parseDouble(initialStop.get("realX")), Double.parseDouble(initialStop.get("realY")));
						TPoint endPoint = new TPoint(Double.parseDouble(endStop.get("realX")), Double.parseDouble(endStop.get("realY")));
						TPoint p = new TPoint((initialPoint.getX() + endPoint.getX()) / 2, (initialPoint.getY() + endPoint.getY()) / 2);
						long initialTime = (long) (Double.parseDouble(initialStop.get("realTime")) * 1000);
						long endTime = (long) (Double.parseDouble(endStop.get("realTime")) * 1000);
						ret.put(stopId, new Stop(stopId.hashCode(), endStop.get("mode"), initialTime, endTime, initialPoint, startStopIndex, endPoint, 2, p, null));
						currentStop.clear();
						startStopIndex = 0;
					}
					currentStop.clear();
					startStopIndex = 0;
				}
				pointIndex++;
			}
			if(!currentStop.isEmpty()) {
				CSVRecord lastRecord = currentStop.get(currentStop.size() - 1);
				CSVRecord initialStop = currentStop.get(0);
				CSVRecord endStop = currentStop.get(currentStop.size() - 1);
				String stopId = lastRecord.get("scenarioID") + "_" + lastRecord.get("MOid") + "_" +lastRecord.get("episId");
				TPoint initialPoint = new TPoint(Double.parseDouble(initialStop.get("realX")), Double.parseDouble(initialStop.get("realY")));
				TPoint endPoint = new TPoint(Double.parseDouble(endStop.get("realX")), Double.parseDouble(endStop.get("realY")));
				TPoint p = new TPoint((initialPoint.getX() + endPoint.getX()) / 2, (initialPoint.getY() + endPoint.getY()) / 2);
				long initialTime = (long) (Double.parseDouble(initialStop.get("realTime")) * 1000);
				long endTime = (long) (Double.parseDouble(endStop.get("realTime")) * 1000);
				ret.put(stopId, new Stop(stopId.hashCode(), endStop.get("mode"), initialTime, endTime, initialPoint, startStopIndex, endPoint, 2, p, null));
				currentStop.clear();
			}
		}
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Map<String, Stop> stops, Map<String, Move> moves, Multimap<String, HermoupolisRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantic_counter);
			Collection<HermoupolisRecord> collection = records.get(trajId);
			int i = 0;
			for (HermoupolisRecord record : collection) {
				TPoint point = new TPoint(record.getRealX(), record.getRealY());
				if(record.getStopId() != null) {
					Stop stop = stops.get(record.getScenario() + "_" + record.getMoid() + "_" + record.getStopId());
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
					s.addData(i, MPID, record.getMpid());
					s.addData(i, MOID, record.getMoid());
					s.addData(i, MODE, record.getTransportationMode());
					s.addData(i, ACTIVITY, record.getActivity());
					s.addData(i, SEMANTIC_TRAJECTORY_TAG, record.getSem_traj_tag());
					i++;
				} else if(record.getMoveId() != null) {
					Move move = moves.get(record.getScenario() + "_" + record.getMoid() + "_" +record.getMoveId());
					if(move == null) {
						throw new RuntimeException("Move does not found");
					}
					move.getStart().setNextMove(move);
					move.getEnd().setPreviousMove(move);
					move.setAttribute(AttributeType.MOVE_TRANSPORTATION_MODE, record.getTransportationMode());
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					move.setAttribute(AttributeType.TRAJECTORY, s);
					s.addData(i, MPID, record.getMpid());
					s.addData(i, MOID, record.getMoid());
					s.addData(i, MODE, record.getTransportationMode());
					s.addData(i, ACTIVITY, record.getActivity());
					s.addData(i, SEMANTIC_TRAJECTORY_TAG, record.getSem_traj_tag());
				}
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantic_counter);
			Collection<HermoupolisRecord> collection = records.get(trajId);
			int i = 0;
			for (HermoupolisRecord record : collection) {
				TPoint point = new TPoint(record.getRealX(), record.getRealY());
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, MPID, record.getMpid());
				s.addData(i, MOID, record.getMoid());
				s.addData(i, MODE, record.getTransportationMode());
				s.addData(i, ACTIVITY, record.getActivity());
				s.addData(i, SEMANTIC_TRAJECTORY_TAG, record.getSem_traj_tag());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}
