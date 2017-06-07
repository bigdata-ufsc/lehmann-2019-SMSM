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

public class DublinBusDataReader {
	
	public static final BasicSemantic<Integer> LINE_INFO = new BasicSemantic<>(3);
	public static final BasicSemantic<String> JOURNEY = new BasicSemantic<>(4);
	public static final BasicSemantic<Boolean> CONGESTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<Integer> STOP = new BasicSemantic<>(7);
	public static final BasicSemantic<String> OPERATOR = new BasicSemantic<>(8);
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(9);

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.dublin201301", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet data = st.executeQuery(
				"select gid, to_timestamp(time_in_seconds / 1000000) as \"time\", line_id, trim(journey_pattern) as journey_pattern, "
				/**/+ "vehicle_journey, trim(operator) as operator, congestion, longitude, latitude, block_journey_id, vehicle_id, stop_id, semantic_stop_id "
				+ "from bus.dublin_201301 "
				+ "where date_frame between '2013-01-25' and '2013-01-31'"
//				+ "where vehicle_id =33619 and vehicle_journey = 6393"
				+ "order by time_in_seconds"
				);
		Multimap<Integer, DublinBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
			}
			DublinBusRecord record = new DublinBusRecord(
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getInt("line_id"),
				data.getString("journey_pattern"),
				data.getInt("vehicle_journey"),
				data.getString("operator"),
				data.getBoolean("congestion"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				data.getInt("block_journey_id"),
				data.getInt("vehicle_id"),
				data.getInt("stop_id"),
				stop
			);
			records.put(record.getVehicle_journey(), record);
		}
		st.close();
		System.out.printf("Loaded %d raw trajectories from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		Map<Integer, Stop> stops = new HashMap<>();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 10);
			Collection<DublinBusRecord> collection = records.get(trajId);
			int i = 0;
			for (DublinBusRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.GEOGRAPHIC, new TPoint(record.getLatitude(), record.getLongitude()));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, LINE_INFO, record.getLineId());
				s.addData(i, JOURNEY, record.getJourney_pattern());
				s.addData(i, CONGESTION, record.isCongestion());
				s.addData(i, VEHICLE, record.getVehicle_id());
				s.addData(i, STOP, record.getStop_id());
				s.addData(i, OPERATOR, record.getOperator());
				if(record.getSemanticStopId() != null) {
					Stop stop = stops.get(record.getSemanticStopId());
					if(stop == null) {
						stop = new Stop(s, record.getSemanticStopId());
						stops.put(record.getSemanticStopId(), stop);
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
