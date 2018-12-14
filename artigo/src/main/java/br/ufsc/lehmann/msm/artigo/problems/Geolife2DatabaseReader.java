package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;

public class Geolife2DatabaseReader implements IDataReader {
	
	private static final SpatialDistanceFunction GEO_DISTANCE_FUNCTION = new EuclideanDistanceFunction();

	private static final SpatialDistanceFunction DISTANCE_FUNCTION = GEO_DISTANCE_FUNCTION;
	
	private static int semantic_counter = 3;
	
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> TRANSPORTATION_MODE = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> REGION_INTEREST = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> DIRECTION = new BasicSemantic<>(semantic_counter++);
	public static final BasicSemantic<String> PATH = new BasicSemantic<>(semantic_counter++);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(semantic_counter, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(semantic_counter, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, GEO_DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(semantic_counter++, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(semantic_counter, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(semantic_counter, new AttributeDescriptor<Move, Number>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(semantic_counter, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(GEO_DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(semantic_counter++, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(GEO_DISTANCE_FUNCTION)));
	
	public static final BasicSemantic<String> PATH_WITH_DIRECTION = new BasicSemantic<String>(semantic_counter++) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			String dir = DIRECTION.getData(p, i);
			if(StringUtils.isEmpty(dir)) {
				return null;
			}
			return dir + "/" + PATH.getData(p, i);
		}
	};

	private boolean onlyStops;

	private String pointsTable;
	private String moveTable;
	private String stopTable;

	public Geolife2DatabaseReader(boolean onlyStops, String stopTable, String moveTable, String pointsTable) {
		this.onlyStops = onlyStops;
		this.stopTable = stopTable;
		this.moveTable = moveTable;
		this.pointsTable = pointsTable;
	}

	@Override
	public List<SemanticTrajectory> read() {
		try {
			return read(null);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<SemanticTrajectory> read(String[] zones) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, pointsTable, null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time, \"POI\" as poi " + //
						"FROM "
						+ stopTable);
		Map<Integer, Stop> stops = new HashMap<>();
		while (stopsData.next()) {
			int stopId = stopsData.getInt("stop_id");
			Stop stop = stops.get(stopId);
			if (stop == null) {
				stop = new Stop(stopId, null, //
						stopsData.getTimestamp("start_time").getTime(), //
						stopsData.getTimestamp("end_time").getTime(), //
						new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")), //
						stopsData.getInt("begin"), //
						new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), //
						stopsData.getInt("length"), //
						new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")),//
						stopsData.getString("poi"),//
						null//
				);
				stops.put(stopId, stop);
			}
		}
		Map<Integer, Move> moves = new HashMap<>();
		ResultSet movesData = st.executeQuery(
				"SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length " + //
						"FROM "
						+ moveTable);
		while(movesData.next()) {
			int moveId = movesData.getInt("move_id");
				Move move = moves.get(moveId);
			if (move == null) {
				int startStopId = movesData.getInt("start_stop_id");
				if (movesData.wasNull()) {
					startStopId = -1;
				}
				int endStopId = movesData.getInt("end_stop_id");
				if (movesData.wasNull()) {
					endStopId = -1;
				}
				move = new Move(moveId, //
						stops.get(startStopId), //
						stops.get(endStopId), //
						movesData.getTimestamp("start_time").getTime(), //
						movesData.getTimestamp("end_time").getTime(), //
						movesData.getInt("begin"), //
						movesData.getInt("length"), //
						null);
				moves.put(moveId, move);
			}
		}
		st.close();
		List<SemanticTrajectory> ret = null;
		List<Move> usedMoves = new ArrayList<Move>();
		if(onlyStops) {
			ret = readStopsTrajectories(conn, stops, moves, usedMoves);
		} else {
			ret = readRawPoints(conn, stops, moves);
		}
		compute(usedMoves);
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves, List<Move> usedMoves) throws SQLException {
		//
		String sql = "select gps.tid, gps.gid, gps.time, gps.lon, gps.lat, gps.folder_id as user_id,  " + 
				"gps.\"POI\" as poi, " + // 
				"gps.direction, " + // 
				"gps.path, " + //
				"gps.semantic_stop_id, " + //
				"gps.semantic_move_id " + //
		"from " + pointsTable + " gps " + //
		" ";//
		sql += "order by gps.tid, gps.time, gps.gid";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
			}
			Integer move = data.getInt("semantic_move_id");
			if(data.wasNull()) {
				move = null;
			}
			GeolifeRecord record = new GeolifeRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getInt("user_id"),
				null,
				data.getString("poi"),
				data.getString("path"),
				data.getString("direction"),
				stop,
				move
			);
			records.put(record.getTid(), record);
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantic_counter);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			Move currentMove = null;
			for (GeolifeRecord record : collection) {
				if(record.getSemanticStop() == null && record.getSemanticMoveId() == null) {
					continue;
				}
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				if(record.getSemanticStop() != null) {
					if(currentMove != null) {
						if(currentMove.getEnd() != null) {
							TPoint[] points = (TPoint[]) currentMove.getAttribute(AttributeType.MOVE_POINTS);
							List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
							a.add(currentMove.getEnd().getStartPoint());
							currentMove.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
						}
						currentMove = null;
					}
					Stop stop = stops.get(record.getSemanticStop());
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
					s.addData(i, Semantic.SPATIAL, point);
				}
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, USER_ID, record.getUserId());
				s.addData(i, TRANSPORTATION_MODE, record.getTransportationMode());
				s.addData(i, REGION_INTEREST, record.getPOI());
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, PATH, record.getPath());
				s.addData(i, PATH_WITH_DIRECTION, PATH_WITH_DIRECTION.getData(s, i));
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(Connection conn, Map<Integer, Stop> stops,
			Map<Integer, Move> moves) throws SQLException {
		String sql = "select gps.tid, gps.gid, gps.time, gps.lon, gps.lat, gps.folder_id as user_id,  " + 
				"gps.\"POI\" as poi, " + // 
				"gps.direction, " + // 
				"gps.path, " + //
				"gps.semantic_stop_id, " + //
				"gps.semantic_move_id " + //
		"from " + pointsTable + " gps " + //
		"  ";//
		sql += "order by gps.tid, gps.time, gps.gid";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
			}
			Integer move = data.getInt("semantic_move_id");
			if(data.wasNull()) {
				move = null;
			}
			GeolifeRecord record = new GeolifeRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getInt("user_id"),
				null,
				data.getString("poi"),
				data.getString("path"),
				data.getString("direction"),
				stop,
				move
			);
			records.put(record.getTid(), record);
		}
		
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantic_counter);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					stop.setRegion(record.getPOI());
				}
				if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, MOVE_ANGLE_SEMANTIC, move);
				}
				s.addData(i, TRANSPORTATION_MODE, record.getTransportationMode());
				s.addData(i, REGION_INTEREST, record.getPOI());
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, PATH, record.getPath());
				s.addData(i, PATH_WITH_DIRECTION, PATH_WITH_DIRECTION.getData(s, i));
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
