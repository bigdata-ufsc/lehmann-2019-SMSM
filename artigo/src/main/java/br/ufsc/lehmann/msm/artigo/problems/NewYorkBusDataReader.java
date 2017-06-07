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
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class NewYorkBusDataReader {
	
	public static final BasicSemantic<Double> DISTANCE = new BasicSemantic<>(3);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DIRECTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PHASE = new BasicSemantic<>(7);
	public static final BasicSemantic<Double> NEXT_STOP_DISTANCE = new BasicSemantic<>(8);
	public static final BasicSemantic<String> NEXT_STOP_ID = new BasicSemantic<>(9);
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(10);

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet data = st.executeQuery(
				"select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
				/**/+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
				/**/+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id, semantic_stop_id "
				+ "from bus.nyc_20140927 "
				+ "where infered_trip_id is not null "
				+ "order by time_received"
				);
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
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
				stop
			);
			records.put(record.getTripId(), record);
		}
		st.close();
		System.out.printf("Loaded %d raw trajectories from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		Map<Integer, Stop> stops = new HashMap<>();
		int trajectoryId = 0;
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajectoryId++, 11);
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
					if(stop == null) {
						stop = new Stop(s, record.getSemanticStop());
						stops.put(record.getSemanticStop(), stop);
					}
					s.addData(i, STOP_SEMANTIC, stop);
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
