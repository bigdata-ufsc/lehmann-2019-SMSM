package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeRecord;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;

class Geolife2DatabaseReader implements IDataReader {
	
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(3);

	private String pointsTable;

	public Geolife2DatabaseReader() {
		pointsTable = "public.geolife2";
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
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.geolife2_limited", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		List<SemanticTrajectory> ret = null;
		ret = readRawPoints(conn);
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(Connection conn) throws SQLException {
		//
		String sql = "select tid, gid, time, lon, lat, folder_id as user_id  "
				+ "from " + pointsTable//
				+ "  ";//
		sql += "order by tid, time, gid";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setFetchSize(1000);
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys(20000).linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			GeolifeRecord record = new GeolifeRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getInt("user_id"),
				null,
				null,
				null,
				null
			);
			records.put(record.getTid(), record);
		}
		
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 12);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				Timestamp time = record.getTime();
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getGid(), record.getLatitude(), record.getLongitude(), time);
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(time.getTime()), Instant.ofEpochMilli(time.getTime())));
				s.addData(i, USER_ID, record.getUserId());
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}
