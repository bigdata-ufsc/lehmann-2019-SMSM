package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Array;
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

import org.apache.commons.collections4.CollectionUtils;
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
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.msm.artigo.StopMoveSemantic;
import br.ufsc.lehmann.stopandmove.LatLongDistanceFunction;
import br.ufsc.lehmann.stopandmove.angle.AngleInference;
import br.ufsc.lehmann.stopandmove.movedistance.MoveDistance;

public class SanFranciscoCabDataReader {
	
	private static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();
	public static final BasicSemantic<Integer> TID = new BasicSemantic<>(3);
	public static final BasicSemantic<Integer> OCUPATION = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> ROAD = new BasicSemantic<>(5);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(6, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(6, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(7, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(7, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(7, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION, 10)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(7, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance()));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));
	private Integer[] roads;
	private boolean mall;
	private boolean airport;
	private boolean onlyStops;
	
	public SanFranciscoCabDataReader(boolean onlyStop) {
		this.onlyStops = onlyStop;
	}

	public SanFranciscoCabDataReader(boolean onlyStop, Integer[] roads, boolean airport, boolean mall) {
		this(onlyStop);
		this.roads = roads;
		this.airport = airport;
		this.mall = mall;
	}

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.sanfrancisco_taxicab_crawdad", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time, street " + //
						"FROM stops_moves.taxi_sanfrancisco_stop");
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
						stopsData.getString("street")//
				);
				stops.put(stopId, stop);
			}
		}
		Map<Integer, Move> moves = new HashMap<>();
		ResultSet movesData = st.executeQuery(
				"SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length, angle " + //
						"FROM stops_moves.taxi_sanfrancisco_move");
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
				Stop startStop = stops.get(startStopId);
				Stop endStop = stops.get(endStopId);
				move = new Move(moveId, //
						startStop, //
						endStop, //
						movesData.getTimestamp("start_time").getTime(), //
						movesData.getTimestamp("end_time").getTime(), //
						movesData.getInt("begin"), //
						movesData.getInt("length"), //
						null);
				moves.put(moveId, move);
			}
		}
		st.close();
		List<Move> allMoves = new ArrayList<>(moves.values());
		List<SemanticTrajectory> ret = null;
		if(onlyStops) {
			ret = readStopsTrajectories(conn, stops, moves);
		} else {
			ret = loadRawPoints(conn, stops, moves);
		}
		compute(CollectionUtils.removeAll(allMoves, moves.values()));
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws SQLException {
		String sql = "SELECT gid, tid, taxi_id, lat, lon, \"timestamp\", ocupation, airport, mall, road, semantic_stop_id, semantic_move_id" + //
				" FROM taxi.sanfrancisco_taxicab_crawdad";
		if(roads != null) {
			sql += " where road in (SELECT * FROM unnest(?))";
			sql += " and airport = " + Boolean.toString(airport);
			sql += " and mall = " + Boolean.toString(mall);
		}
		sql +=" order by tid, \"timestamp\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(roads != null) {
			Array array = conn.createArrayOf("integer", roads);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, SanFranciscoCabRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			SanFranciscoCabRecord record = new SanFranciscoCabRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getInt("taxi_id"),
				data.getTimestamp("timestamp"),
				data.getInt("ocupation"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getBoolean("airport"),
				data.getBoolean("mall"),
				data.getInt("road"),
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, 8);
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
							Move move = new Move(-1, previousStop, stop, (double) previousStop.getEndTime(), (double) stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									AngleInference.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									MoveDistance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
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
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Loaded %d trajectories (filtered)\n", ret.size());
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> loadRawPoints(Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws SQLException {
		String sql = "SELECT gid, tid, taxi_id, lat, lon, \"timestamp\", ocupation, airport, mall, road, semantic_stop_id, semantic_move_id" + //
				" FROM taxi.sanfrancisco_taxicab_crawdad";
		if(roads != null) {
			sql += " where road in (SELECT * FROM unnest(?))";
			sql += " and airport = " + Boolean.toString(airport);
			sql += " and mall = " + Boolean.toString(mall);
		}
		sql +=" order by tid, \"timestamp\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(roads != null) {
			Array array = conn.createArrayOf("integer", roads);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, SanFranciscoCabRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			SanFranciscoCabRecord record = new SanFranciscoCabRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getInt("taxi_id"),
				data.getTimestamp("timestamp"),
				data.getInt("ocupation"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getBoolean("airport"),
				data.getBoolean("mall"),
				data.getInt("road"),
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, 8);
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
			move.setAttribute(AttributeType.MOVE_ANGLE, AngleInference.getAngle(points.get(0), points.get(points.size() - 1)));
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, MoveDistance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION));
		}
	}
}
