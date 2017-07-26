package br.ufsc.lehmann.segmentation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class TDrive {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.shangai_20070220", null, "geom");
		Map<Integer, String> registers = new HashMap<>();
		Connection conn = source.getRetriever().getConnection();
		PreparedStatement ps = conn.prepareStatement("SELECT tid, min(\"time\") as initial_time FROM taxi.\"beijing_t-drive\" group by tid");
		ResultSet rs = ps.executeQuery();
		LocalTime morning = LocalTime.of(6, 0);
		LocalTime afternoon = LocalTime.of(12, 0);
		LocalTime evening = LocalTime.of(18, 0);
		LocalTime night = LocalTime.of(0, 0);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT+8")));
		while(rs.next()) {
			Timestamp timestamp = rs.getTimestamp("initial_time", calendar);
			LocalTime t = LocalTime.from(timestamp.toInstant().atZone(ZoneId.of("GMT+8")));
			String time = "";
			if(t.isAfter(evening)) {
				time = "EVENING";
			} else if(t.isAfter(afternoon)) {
				time = "AFTERNOON";
			} else if(t.isAfter(morning)) {
				time = "MORNING";
			} else if(t.isAfter(night)) {
				time = "NIGHT";
			}
			registers.put(rs.getInt("tid"), time);
		}
		System.out.println("Total trajectories count: " + registers.keySet().size());
		rs.close();
		ps.close();
		conn.setAutoCommit(false);
		ps = conn.prepareStatement("update taxi.\"beijing_t-drive\" set time_of_day = ? where tid = ?");
		Set<Integer> tids = registers.keySet();
		for (Integer tid : tids) {
			String time = registers.get(tid);
			ps.setString(1, time);
			ps.setInt(2, tid);
			ps.addBatch();
			if(tid % 100 == 0) {
				System.out.println("Current tid: " + tid);
				ps.executeBatch();
				conn.commit();
			}
		}
		ps.executeBatch();
		conn.commit();
	}
}
