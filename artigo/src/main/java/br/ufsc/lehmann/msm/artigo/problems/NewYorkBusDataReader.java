package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.stopandmove.LatLongDistanceFunction;

public class NewYorkBusDataReader {
	
	public static final BasicSemantic<Double> DISTANCE = new BasicSemantic<>(3);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DIRECTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PHASE = new BasicSemantic<>(7);
	public static final BasicSemantic<Double> NEXT_STOP_DISTANCE = new BasicSemantic<>(8);
	public static final BasicSemantic<String> NEXT_STOP_ID = new BasicSemantic<>(9);
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(10, new LatLongDistanceFunction());
	public static final MoveSemantic MOVE_SEMANTIC = new MoveSemantic(11);

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time " + //
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
						new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon"))//
				);
				stops.put(stopId, stop);
			}
		}
		Map<Integer, Move> moves = new HashMap<>();
		ResultSet movesData = st.executeQuery(
				"SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length " + //
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
						movesData.getInt("length"));
				moves.put(moveId, move);
			}
		}
		ResultSet data = st.executeQuery(
				"select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
				/**/+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
				/**/+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id, semantic_stop_id, semantic_move_id "
				+ "from bus.nyc_20140927 "//
				+ "where infered_trip_id is not null "//
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
				+ "order by time_received"//
				);
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
		st.close();
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
				s.addData(i, Semantic.GEOGRAPHIC, new TPoint(record.getLatitude(), record.getLongitude()));
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
					s.addData(i, STOP_SEMANTIC, stop);
				}
				if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					s.addData(i, MOVE_SEMANTIC, move);
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
