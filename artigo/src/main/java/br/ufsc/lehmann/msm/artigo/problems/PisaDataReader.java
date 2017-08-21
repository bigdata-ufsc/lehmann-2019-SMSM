package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import br.ufsc.lehmann.stopandmove.LatLongDistanceFunction;

public class PisaDataReader {
	
	public static final BasicSemantic<Double> ELEVATION = new BasicSemantic<>(3);
	public static final BasicSemantic<String> WEATHER = new BasicSemantic<>(4);
	public static final BasicSemantic<Double> TEMPERATURE = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PLACE = new BasicSemantic<>(7);
	public static final BasicSemantic<String> GOAL = new BasicSemantic<>(8);
	public static final BasicSemantic<String> SUBGOAL = new BasicSemantic<>(9);
	public static final BasicSemantic<String> TRANSPORTATION = new BasicSemantic<>(10);
	public static final BasicSemantic<String> EVENT = new BasicSemantic<>(11);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(12, new AttributeDescriptor<Stop>(AttributeType.STOP_CENTROID, new LatLongDistanceFunction()));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(12, new AttributeDescriptor<Stop>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(13, new AttributeDescriptor<Move>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(13, new AttributeDescriptor<Move>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(13, new AttributeDescriptor<Move>(AttributeType.MOVE_POINTS, new DTWDistance(new LatLongDistanceFunction(), 10)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(13, new AttributeDescriptor<Move>(AttributeType.MOVE_POINTS, new EllipsesDistance()));

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "pisa", DataSourceType.PGSQL, "public.semanticpoint", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
						"centroid_lon, start_time, end_time, street " + //
						"FROM stops_moves.pisa_stop");
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
						"FROM stops_moves.pisa_move");
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
						null,
						movesData.getDouble("angle"));
				moves.put(moveId, move);
			}
		}

		String sql = "select gid, tid, \"time\", is_stop, geom, lat, lon, ele, weather, temperature, "
        + "user_id, place, goal, subgoal, transportation, event, dailytid, semantic_stop_id, semantic_move_id "
		+ "from public.semanticpoint ";
		sql += "order by tid, dailytid, \"time\"";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		ResultSet data = preparedStatement.executeQuery();
		Multimap<String, PisaRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			PisaRecord record = new PisaRecord(
					data.getTimestamp("time"),
					data.getInt("is_stop"),
					data.getInt("user_id"),
				data.getInt("gid"),
				data.getInt("tid"),
				data.getInt("daily_tid"),
				data.getDouble("lat"),
				data.getDouble("lon"),
				data.getDouble("ele"),
				data.getDouble("temperature"),
				data.getString("weather"),
				data.getString("place"),
				data.getString("goal"),
				data.getString("subgoal"),
				data.getString("transportation"),
				data.getString("event"),
				stop,
				move
			);
			records.put(record.getTid() + "" + record.getDaily_tid(), record);
		}
		st.close();
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 14);
			Collection<PisaRecord> collection = records.get(trajId);
			int i = 0;
			for (PisaRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLat(), record.getLon());
				s.addData(i, Semantic.GEOGRAPHIC, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, ELEVATION, record.getEle());
				s.addData(i, WEATHER, record.getWeather());
				s.addData(i, TEMPERATURE, record.getTemperature());
				s.addData(i, USER_ID, record.getUser_id());
				s.addData(i, PLACE, record.getPlace());
				s.addData(i, GOAL, record.getGoal());
				s.addData(i, SUBGOAL, record.getSubGoal());
				s.addData(i, TRANSPORTATION, record.getTransportation());
				s.addData(i, EVENT, record.getEvent());
				if(record.getSemanticStopId() != null) {
					Stop stop = stops.get(record.getSemanticStopId());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
				}
				if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					((List<TPoint>) move.getAttribute(AttributeType.MOVE_POINTS)).add(point);
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
}
