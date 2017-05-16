package br.ufsc.lehmann.msm.artigo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class DublinBusDataReader {
	
	public static final BasicSemantic<Integer> LINE_INFO = new BasicSemantic<>(2);
	public static final BasicSemantic<String> JOURNEY = new BasicSemantic<>(3);
	public static final BasicSemantic<Boolean> CONGESTION = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> STOP = new BasicSemantic<>(6);
	public static final BasicSemantic<String> OPERATOR = new BasicSemantic<>(7);

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.dublin201301", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet data = st.executeQuery(
				"select to_timestamp(time_in_seconds / 1000000) as \"time\", line_id, journey_pattern, "
				/**/+ "vehicle_journey, operator, congestion, longitude, latitude, block_journey_id, vehicle_id, stop_id "
				+ "from bus.dublin_201301 where at_stop = true");
		Multimap<Integer, DublinBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			DublinBusRecord record = new DublinBusRecord(
				data.getDate("time"),
				data.getInt("line_id"),
				data.getString("journey_pattern"),
				data.getInt("vehicle_journey"),
				data.getString("operator"),
				data.getBoolean("congestion"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				data.getInt("block_journey_id"),
				data.getInt("vehicle_id"),
				data.getInt("stop_id")
			);
			records.put(record.getVehicle_journey(), record);
		}
		st.close();
		System.out.printf("Loaded %d raw trajectories from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 8);
			Collection<DublinBusRecord> collection = records.get(trajId);
			int i = 0;
			for (DublinBusRecord record : collection) {
				s.addData(i, Semantic.GEOGRAPHIC, new TPoint(record.getLatitude(), record.getLongitude()));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, LINE_INFO, record.getLineId());
				s.addData(i, JOURNEY, record.getJourney_pattern());
				s.addData(i, CONGESTION, record.isCongestion());
				s.addData(i, VEHICLE, record.getVehicle_id());
				s.addData(i, STOP, record.getStop_id());
				s.addData(i, OPERATOR, record.getOperator());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}
