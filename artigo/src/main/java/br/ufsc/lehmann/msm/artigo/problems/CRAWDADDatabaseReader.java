package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class CRAWDADDatabaseReader implements IDataReader {
	
	private static int SEMANTIC_COUNTER = 3;
	
	public static final BasicSemantic<Integer> TID = new BasicSemantic<>(SEMANTIC_COUNTER++);
	public static final BasicSemantic<String> GROUP = new BasicSemantic<>(SEMANTIC_COUNTER++);
	
	public List<SemanticTrajectory> read(){
		try {
			DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.sanfrancisco_taxicab", null, null);
			DataRetriever retriever = source.getRetriever();
			System.out.println("Executing SQL...");
			Connection conn = retriever.getConnection();
			List<SemanticTrajectory> ret = null;
			try {

				conn.setAutoCommit(false);
				Statement st = conn.createStatement();
				st.setFetchSize(1000);

				ret = loadRawPoints(conn);
			} finally {
				conn.close();			
			}
			return ret;
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private List<SemanticTrajectory> loadRawPoints(Connection conn) throws SQLException {
		String sql = "SELECT gid, tid, x, y, "
				+ "\"time\", \"group\"" + //
				" FROM public.crawdad order by tid, \"time\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, CRAWDADRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			CRAWDADRecord record = new CRAWDADRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getDouble("x"),
				data.getDouble("y"),
				data.getTimestamp("time"),
				data.getString("group")
			);
			records.put(record.getTid(), record);
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, SEMANTIC_COUNTER);
			Collection<CRAWDADRecord> collection = records.get(trajId);
			int i = 0;
			for (CRAWDADRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getX(), record.getY());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, TID, record.getTid());
				s.addData(i, GROUP, record.getGroup());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Loaded %d trajectories (filtered)\n", ret.size());
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}
