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
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.LatLongDistanceFunction;

public class NewYorkBusDatabaseReader {
	
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
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(11, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));
	private boolean onlyStops;

	public NewYorkBusDatabaseReader(boolean onlyStops) {
		this.onlyStops = onlyStops;
	}

	public List<SemanticTrajectory> read(String[] lines) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time, street " + //
						"FROM stops_moves.bus_nyc_20140927_stop");
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
						"FROM stops_moves.bus_nyc_20140927_move");
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
			ret = readStopsTrajectories(lines, conn, stops, moves, usedMoves);
		} else {
			ret = readRawPoints(lines, conn, stops, moves);
		}
		compute(usedMoves);
		return ret;
	}

	public List<Stop> exportStops(String... lines) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);

		String sql = "SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
				"centroid_lon, start_time, end_time, street " + //
				"FROM stops_moves.bus_nyc_20140927_stop";
		if(lines != null && lines.length > 0) {
			sql += " where stop_id in (select semantic_stop_id from bus.nyc_20140927 where trim(infered_route_id) in (SELECT * FROM unnest(?))) ";
		}
		PreparedStatement st = conn.prepareStatement(sql);
		st.setFetchSize(1000);
		if(lines != null && lines.length > 0) {
			Array array = conn.createArrayOf("varchar", lines);
			st.setArray(1, array);
		}
		ResultSet stopsData = st.executeQuery();
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
		return new ArrayList<>(stops.values());
	}

	private List<SemanticTrajectory> readStopsTrajectories(String[] lines, Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves, List<Move> usedMoves) throws SQLException {
		String sql = "select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
		/**/+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
		/**/+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id, semantic_stop_id, semantic_move_id "
		+ "from bus.nyc_20140927 "//
		+ "where infered_trip_id is not null ";//
		if(lines != null && lines.length > 0) {
			sql += "and infered_route_id in (SELECT * FROM unnest(?)) ";
		}
		sql += "order by time_received";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(lines != null && lines.length > 0) {
			Array array = conn.createArrayOf("varchar", lines);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			NewYorkBusRecord record = new NewYorkBusRecord(
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getInt("vehicle_id"),
				data.getString("route"),
				data.getString("trip_id"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				data.getDouble("distance_along_trip"),
				data.getInt("infered_direction_id"),
				data.getString("phase"),
				data.getDouble("next_scheduled_stop_distance"),
				data.getString("next_scheduled_stop_id"),
				stop,
				move
			);
			records.put(record.getTripId(), record);
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
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
				}
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
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

	private List<SemanticTrajectory> readRawPoints(String[] lines, Connection conn, Map<Integer, Stop> stops,
			Map<Integer, Move> moves) throws SQLException {
		String sql = "select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
		/**/+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
		/**/+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id, semantic_stop_id, semantic_move_id "
		+ "from bus.nyc_20140927 "//
		+ "where infered_trip_id is not null ";//
		//2 linhas
//				+ "and ('MTA NYCT_Q20A'=infered_route_id or 'MTA NYCT_M102'=infered_route_id) "
		//5 linhas
//				+ "and ('MTA NYCT_Q20A'=infered_route_id or 'MTA NYCT_Q13'=infered_route_id or 'MTABC_Q66'=infered_route_id or 'MTABC_Q65'=infered_route_id or 'MTA NYCT_Q32'=infered_route_id) "
		//10 linhas
//				+ "and ('MTA NYCT_Q20A'=infered_route_id or 'MTA NYCT_Q13'=infered_route_id or 'MTABC_Q66'=infered_route_id or 'MTABC_Q65'=infered_route_id or 'MTA NYCT_Q32'=infered_route_id or 'MTA NYCT_M42'=infered_route_id or 'MTABC_Q49'=infered_route_id or 'MTA NYCT_Q28'=infered_route_id or 'MTA NYCT_X10'=infered_route_id or 'MTA NYCT_M102'=infered_route_id) "
		//50 menores linhas
//				+ "and ('MTABC_Q34'=infered_route_id or 'MTA NYCT_X28'=infered_route_id or 'MTA NYCT_Q20B'=infered_route_id or 'MTA NYCT_S42'=infered_route_id or 'MTA NYCT_S66'=infered_route_id or "
//				+ "'MTABC_BM5'=infered_route_id or 'MTABC_BM4'=infered_route_id or 'MTABC_BM1'=infered_route_id or 'MTABC_Q67'=infered_route_id or 'MTABC_QM15'=infered_route_id or "
//				+ "'MTABC_BM3'=infered_route_id or 'MTA NYCT_B84'=infered_route_id or 'MTABC_BM2'=infered_route_id or 'MTABC_BXM4'=infered_route_id or 'MTABC_QM5'=infered_route_id or "
//				+ "'MTABC_QM6'=infered_route_id or 'MTABC_QM2'=infered_route_id or 'MTABC_QM4'=infered_route_id or 'MTABC_BXM3'=infered_route_id or 'MTABC_BXM6'=infered_route_id or "
//				+ "'MTA NYCT_M106'=infered_route_id or 'MTABC_Q103'=infered_route_id or 'MTA NYCT_X27'=infered_route_id or 'MTA NYCT_Q31'=infered_route_id or 'MTABC_Q19'=infered_route_id or "
//				+ "'MTABC_BXM8'=infered_route_id or 'MTABC_BXM9'=infered_route_id or 'MTABC_BXM10'=infered_route_id or 'MTABC_Q104'=infered_route_id or 'MTA NYCT_BX18'=infered_route_id or "
//				+ "'MTABC_BXM1'=infered_route_id or 'MTA NYCT_B39'=infered_route_id or 'MTABC_Q101'=infered_route_id or 'MTA NYCT_B32'=infered_route_id or 'MTABC_BXM11'=infered_route_id or "
//				+ "'MTA NYCT_X17'=infered_route_id or 'MTABC_BXM2'=infered_route_id or 'MTA NYCT_B69'=infered_route_id or 'MTA NYCT_BX46'=infered_route_id or 'MTA NYCT_M50'=infered_route_id or "
//				+ "'MTA NYCT_M12'=infered_route_id or 'MTA NYCT_S57'=infered_route_id or 'MTA NYCT_BX24'=infered_route_id or 'MTA NYCT_Q76'=infered_route_id or 'MTA NYCT_BX4A'=infered_route_id or "
//				+ "'MTABC_BX23'=infered_route_id or 'MTA NYCT_BX8'=infered_route_id or 'MTA NYCT_B74'=infered_route_id or 'MTA NYCT_Q15A'=infered_route_id) "
		if(lines != null && lines.length > 0) {
			sql += "and infered_route_id in (SELECT * FROM unnest(?)) ";
		}
		sql += "order by time_received";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(lines != null && lines.length > 0) {
			Array array = conn.createArrayOf("varchar", lines);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			NewYorkBusRecord record = new NewYorkBusRecord(
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getInt("vehicle_id"),
				data.getString("route"),
				data.getString("trip_id"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				data.getDouble("distance_along_trip"),
				data.getInt("infered_direction_id"),
				data.getString("phase"),
				data.getDouble("next_scheduled_stop_distance"),
				data.getString("next_scheduled_stop_id"),
				stop,
				move
			);
			records.put(record.getTripId(), record);
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
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
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, Distance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION));
		}
	}
}
