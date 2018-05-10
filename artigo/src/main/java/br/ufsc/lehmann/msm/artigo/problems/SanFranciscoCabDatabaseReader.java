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
import org.apache.commons.lang3.ArrayUtils;
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
import br.ufsc.utils.LatLongDistanceFunction;

public class SanFranciscoCabDatabaseReader {
	
	private static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();
	public static final BasicSemantic<Integer> TID = new BasicSemantic<>(3);
	public static final BasicSemantic<Integer> OCUPATION = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> ROAD = new BasicSemantic<>(5);
	public static final BasicSemantic<String> DIRECTION = new BasicSemantic<>(6);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance()));
	
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
	public static final BasicSemantic<String> ROUTE_WITH_ROADS = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return ROAD.getData(p, i) + "/" + ROUTE.getData(p, i);
		}
	};
	public static final BasicSemantic<String> ROADS_WITH_DIRECTION = new BasicSemantic<String>(6) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return DIRECTION.getData(p, i) + "/" + ROAD.getData(p, i);
		}
	};
	
	private String[] roads;
	private boolean onlyStops;
	private String[] directions;
	private String[] regions;
	
	public SanFranciscoCabDatabaseReader(boolean onlyStop) {
		this.onlyStops = onlyStop;
	}

	public SanFranciscoCabDatabaseReader(boolean onlyStop, String[] roads, String[] directions) {
		this(onlyStop);
		this.roads = roads;
		this.directions = directions;
	}

	public SanFranciscoCabDatabaseReader(boolean onlyStop, String[] roads, String[] directions, String[] regions) {
		this(onlyStop, roads, directions);
		this.regions = regions;
	}

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.sanfrancisco_taxicab", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		List<SemanticTrajectory> ret = null;
		try {

			conn.setAutoCommit(false);
			Statement st = conn.createStatement();
			st.setFetchSize(1000);

			ResultSet stopsData = st.executeQuery(
					"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
							"centroid_lon, start_time, end_time, street, \"POI\" " + //
							"FROM stops_moves.taxi_sanfrancisco_stop");
			Map<Integer, Stop> stops = new HashMap<>();
			while (stopsData.next()) {
				int stopId = stopsData.getInt("stop_id");
				Stop stop = stops.get(stopId);
				if (stop == null) {
					stop = new Stop(stopId, stopsData.getString("POI"), //
							stopsData.getTimestamp("start_time").getTime(), //
							stopsData.getTimestamp("end_time").getTime(), //
							new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")), //
							stopsData.getInt("begin"), //
							new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), //
							stopsData.getInt("length"), //
							new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")), //
							stopsData.getString("street"), //
							stopsData.getString("POI")//
					);
					stops.put(stopId, stop);
				}
			}
			Map<Integer, Move> moves = new HashMap<>();
			ResultSet movesData = st
					.executeQuery("SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length " + //
							"FROM stops_moves.taxi_sanfrancisco_move");
			while (movesData.next()) {
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
			if (onlyStops) {
				ret = readStopsTrajectories(conn, stops, moves);
			} else {
				ret = loadRawPoints(conn, stops, moves);
			}
			compute(CollectionUtils.removeAll(allMoves, moves.values()));
		} finally {
			conn.close();			
		}
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws SQLException {
		String sql = "SELECT gid, tid, taxi_id, lat, lon, \"timestamp\", ocupation, airport, mall, road, direction, stop, semantic_stop_id, semantic_move_id, stop, route" + //
				" FROM taxi.sanfrancisco_taxicab where 1=1";
		if(!ArrayUtils.isEmpty(roads)) {
			sql += " and road in (SELECT * FROM unnest(?))";
		} else if(roads != null) {
			sql += " and road is not null";
		}
		if(!ArrayUtils.isEmpty(directions)) {
			sql += " and direction in (SELECT * FROM unnest(?))";
		} else if(directions != null) {
			sql += " and direction is not null";
		}
		if(regions != null) {
			sql += " and tid in (select distinct r.tid from taxi.sanfrancisco_taxicab r where r.stop in (SELECT * FROM unnest(?)))";
		}
		sql +=" order by tid, \"timestamp\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		int index = 1;
		if(!ArrayUtils.isEmpty(roads)) {
			Array array = conn.createArrayOf("integer", roads);
			preparedStatement.setArray(index++, array);
		}
		if(!ArrayUtils.isEmpty(directions)) {
			Array array = conn.createArrayOf("text", directions);
			preparedStatement.setArray(index++, array);
		}
		if(regions != null) {
			Array array = conn.createArrayOf("text", regions);
			preparedStatement.setArray(index++, array);
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
				data.getString("direction"),
				data.getString("stop"),
				data.getString("route"),
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
						if(previousStop != null && previousStop.getNextMove() == null) {
							Move move = new Move(-1, previousStop, stop, previousStop.getEndTime(), stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									Angle.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
							previousStop.setNextMove(move);
							stop.setPreviousMove(move);
							//injecting a move between two consecutives stops
							stops.put(record.getSemanticStop(), stop);
						}
					}
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
					s.addData(i, Semantic.SPATIAL, stop.getCentroid());
					s.addData(i, Semantic.GID, record.getGid());
					s.addData(i, TID, record.getTid());
					s.addData(i, OCUPATION, record.getOcupation());
					s.addData(i, ROAD, record.getRoad());
					s.addData(i, DIRECTION, record.getDirection());
					s.addData(i, REGION_INTEREST, record.getRegion());
					s.addData(i, ROUTE, record.getRoute());
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
					points = a.toArray(new TPoint[a.size()]);
					move.setAttribute(AttributeType.MOVE_POINTS, points);
				}
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Loaded %d trajectories (filtered)\n", ret.size());
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> loadRawPoints(Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves) throws SQLException {
		String sql = "SELECT gid, tid, taxi_id, lat, lon, \"timestamp\", ocupation, airport, mall, road, direction, semantic_stop_id, semantic_move_id, stop, route" + //
				" FROM taxi.sanfrancisco_taxicab where 1=1";
		if(!ArrayUtils.isEmpty(roads)) {
			sql += " and road in (SELECT * FROM unnest(?))";
		} else if(roads != null) {
			sql += " and road is not null";
		}
		if(!ArrayUtils.isEmpty(directions)) {
			sql += " and direction in (SELECT * FROM unnest(?))";
		} else if(directions != null) {
			sql += " and direction is not null";
		}
		if(regions != null) {
			sql += " and tid in (select distinct r.tid from taxi.sanfrancisco_taxicab r where r.stop in (SELECT * FROM unnest(?)))";
		}
		sql +=" order by tid, \"timestamp\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		int index = 1;
		if(!ArrayUtils.isEmpty(roads)) {
			Array array = conn.createArrayOf("integer", roads);
			preparedStatement.setArray(index++, array);
		}
		if(!ArrayUtils.isEmpty(directions)) {
			Array array = conn.createArrayOf("text", directions);
			preparedStatement.setArray(index++, array);
		}
		if(regions != null) {
			Array array = conn.createArrayOf("text", regions);
			preparedStatement.setArray(index++, array);
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
				data.getString("direction"),
				data.getString("stop"),
				data.getString("route"),
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
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<SanFranciscoCabRecord> collection = records.get(trajId);
			int i = 0;
			for (SanFranciscoCabRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude());
				s.addData(i, Semantic.SPATIAL, point);
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
					stop.setRegion(record.getRegion());
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
