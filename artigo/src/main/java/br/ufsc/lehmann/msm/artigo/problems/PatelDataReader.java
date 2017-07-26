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
import br.ufsc.lehmann.stopandmove.EuclideanDistanceFunction;

public class PatelDataReader {
	
	private String table;
	private String stopMoveTable;

	public PatelDataReader(String table, String stopMoveTable) {
		this.table = table;
		this.stopMoveTable = stopMoveTable;
	}

	public static final BasicSemantic<String> TID = new BasicSemantic<>(3);
	public static final BasicSemantic<String> CLASS = new BasicSemantic<>(4);
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(5, new EuclideanDistanceFunction());
	public static final MoveSemantic MOVE_SEMANTIC = new MoveSemantic(6);

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "patel." + table, null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time " + //
						"FROM stops_moves.patel_" + stopMoveTable + "_stop");
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
				"SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length, angle " + //
						"FROM stops_moves.patel_" + stopMoveTable + "_move");
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
						movesData.getDouble("angle"));
				moves.put(moveId, move);
			}
		}
		
		ResultSet data = st.executeQuery(
				"SELECT tid, class, \"time\", latitude, longitude, gid, semantic_stop_id, " + //
						"semantic_move_id " + //
						"FROM patel." + table + //
						" order by \"time\"");
		Multimap<String, PatelRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			PatelRecord record = new PatelRecord(
					data.getString("tid"),
				data.getInt("gid"),
				data.getDouble("time"),
				data.getString("class"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				stop,
				move
			);
			records.put(record.getTid().trim(), record);
		}
		st.close();
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 7);
			Collection<PatelRecord> collection = records.get(trajId);
			int i = 0;
			for (PatelRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.GEOGRAPHIC, new TPoint(record.getLatitude(), record.getLongitude()));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli((long) record.getTime()), Instant.ofEpochMilli((long) record.getTime())));
				s.addData(i, TID, record.getTid());
				s.addData(i, CLASS, record.getClazz());
				if(record.getStop() != null) {
					Stop stop = stops.get(record.getStop());
					s.addData(i, STOP_SEMANTIC, stop);
				}
				if(record.getMove() != null) {
					Move move = moves.get(record.getMove());
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
