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
import br.ufsc.core.trajectory.SpatialDistanceFunction;
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
import br.ufsc.utils.EuclideanDistanceFunction;

public class GeolifeUniversityDatabaseReader {
	
	private static final int SAMPLING_RATE = 1;
	
	private static final SpatialDistanceFunction GEO_DISTANCE_FUNCTION = new EuclideanDistanceFunction();

	private static final SpatialDistanceFunction DISTANCE_FUNCTION = GEO_DISTANCE_FUNCTION;
	
	private static int SEMANTICS_COUNTER = 3;
	
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> TRANSPORTATION_MODE = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> REGION_INTEREST = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> PATH = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final BasicSemantic<String> DIRECTION = new BasicSemantic<>(SEMANTICS_COUNTER++);
	public static final StopSemantic STOP_REGION_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Stop, String>(AttributeType.STOP_REGION, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, GEO_DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(SEMANTICS_COUNTER++, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(GEO_DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(SEMANTICS_COUNTER++, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(GEO_DISTANCE_FUNCTION)));
	
	public static final StopMoveSemantic STOP_MOVE_COMBINED = new StopMoveSemantic(STOP_STREET_NAME_SEMANTIC, MOVE_ANGLE_SEMANTIC, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));

	private boolean onlyStops;
	private boolean withTransportation;

	public GeolifeUniversityDatabaseReader(boolean onlyStops) {
		this(onlyStops, false);
	}

	public GeolifeUniversityDatabaseReader(boolean onlyStops, boolean withTransportation) {
		this.onlyStops = onlyStops;
		this.withTransportation = withTransportation;
	}

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.geolife_with_pois_university_stop", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		List<SemanticTrajectory> ret = null;
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement();
			st.setFetchSize(1000);

			String stopTable = this.withTransportation ? "stops_moves.geolife_enriched_stop"
					: "stops_moves.geolife2_limited_stop";
			String moveTable = this.withTransportation ? "stops_moves.geolife_enriched_move"
					: "stops_moves.geolife2_limited_move";
			//
			stopTable = "stops_moves.geolife_inside_university_stop_5_pois";
			moveTable = "stops_moves.geolife_inside_university_move_5_pois";
			//
			ResultSet stopsData = st.executeQuery(
					"SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
							"centroid_lon, start_time, end_time, street, \"POI\" " + //
							"FROM " + stopTable);
			Map<Integer, Stop> stops = new HashMap<>();
			while (stopsData.next()) {
				int stopId = stopsData.getInt("stop_id");
				Stop stop = stops.get(stopId);
				if (stop == null) {
					stop = new Stop(stopId, stopsData.getString("POI"), //
							stopsData.getTimestamp("start_time").getTime(), //
							stopsData.getTimestamp("end_time").getTime(), //
							new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")), //
							stopsData.getInt("begin"), //
							new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), //
							stopsData.getInt("length"), //
							new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")), //
							stopsData.getString("POI"), //
							stopsData.getString("street")//
					);
					stops.put(stopId, stop);
				}
			}
			Map<Integer, Move> moves = new HashMap<>();
			ResultSet movesData = st
					.executeQuery("SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length " + //
							"FROM " + moveTable);
			while (movesData.next()) {
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
			List<Move> usedMoves = new ArrayList<Move>();
			if (onlyStops) {
				ret = readStopsTrajectories(null, conn, stops, moves, usedMoves);
			} else {
				ret = readRawPoints(null, conn, stops, moves);
			}
			compute(usedMoves);
		} finally {
			conn.close();
		}
		return ret;
	}

	public List<Stop> exportStops(String... zones) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "lehmann", DataSourceType.PGSQL, "stops_moves.geolife_with_pois_university_stop", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		String stopTable = this.withTransportation ? "stops_moves.geolife_enriched_stop" : "stops_moves.geolife2_limited_stop";
		String pointsTable = this.withTransportation ? "geolife.geolife_enriched_transportation_means" : "public.geolife2_limited";
		//
		stopTable = "stops_moves.geolife_with_pois_university_stop";
		pointsTable = "geolife.geolife_with_pois_university";
		//
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);

		String sql = "SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
				"centroid_lon, start_time, end_time, street " + //
				"FROM " + stopTable;
		if(zones != null && zones.length > 0) {
			sql += " where stop_id in (select semantic_stop_id from " + pointsTable +  " where trim(\"POI\") in (SELECT * FROM unnest(?))) ";
		}
		PreparedStatement st = conn.prepareStatement(sql);
		st.setFetchSize(1000);
		if(zones != null && zones.length > 0) {
			Array array = conn.createArrayOf("varchar", zones);
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

	private List<SemanticTrajectory> readStopsTrajectories(String[] zones, Connection conn, Map<Integer, Stop> stops, Map<Integer, Move> moves, List<Move> usedMoves) throws SQLException {
		String transportationColumn = this.withTransportation ? "mode" : "transportation_mean";
		String pointsTable = this.withTransportation ? "geolife.geolife_enriched_transportation_means" : "public.geolife2_limited";
		//
		transportationColumn = "'NONE'";
		pointsTable = "geolife.geolife_inside_university_5_pois";
		//
		String sql = "select tid, gid, time, lon, lat, folder_id as user_id, " + transportationColumn + " as transporationMode, \"POI\", semantic_stop_id, semantic_move_id, path, direction "
		+ "from " + pointsTable//
		+ " where 1=1 ";//
		if(zones != null && zones.length > 0) {
			sql += "and POI in (SELECT * FROM unnest(?)) ";
		}
		//
//		sql += "and tid in (32951,18767,20833) ";
		sql += "and (direction, path) in (select direction, path from "+ pointsTable + " group by direction, path having count(distinct tid) > 3) ";
		//
		sql += "order by tid, time, gid";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(zones != null && zones.length > 0) {
			Array array = conn.createArrayOf("varchar", zones);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			GeolifeRecord record = new GeolifeRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getInt("user_id"),
				data.getString("transporationMode"),
				data.getString("POI"),
				data.getString("path"),
				data.getString("direction"),
				stop,
				move
			);
			records.put(record.getTid(), record);
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, SEMANTICS_COUNTER);
			Collection<GeolifeRecord> collection = records.get(trajId);
			int i = 0;
			for (GeolifeRecord record : collection) {
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude(), record.getTime());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					if(stop == null) {
						throw new RuntimeException("Stop does not found");
					}
					stop.addPoint(point);
					if(i > 0) {
						if(STOP_CENTROID_SEMANTIC.getData(s, i - 1) == stop) {
							continue;
						}
						Stop previousStop = STOP_CENTROID_SEMANTIC.getData(s, i - 1);
						if(previousStop != null && previousStop.getNextMove() == null) {
							Move move = new Move(-1, previousStop, stop, previousStop.getEndTime(), stop.getStartTime(), stop.getBegin() - 1, 0, new TPoint[0], 
									Angle.getAngle(previousStop.getEndPoint(), stop.getStartPoint()), 
									Distance.getDistance(new TPoint[] {previousStop.getEndPoint(), stop.getStartPoint()}, DISTANCE_FUNCTION));
							previousStop.setNextMove(move);
							stop.setPreviousMove(move);
						}
					}
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(stop.getStartTime()), Instant.ofEpochMilli(stop.getEndTime())));
					s.addData(i, Semantic.GID, record.getGid());
					s.addData(i, Semantic.SPATIAL_LATLON, stop.getCentroid());
					s.addData(i, USER_ID, record.getUserId());
					s.addData(i, REGION_INTEREST, record.getPOI());
					s.addData(i, PATH, record.getPath());
					s.addData(i, DIRECTION, record.getDirection());
					stop.setRegion(record.getPOI());
					i++;
				} else if(record.getSemanticMoveId() != null) {
					Move move = moves.get(record.getSemanticMoveId());
					if(move == null) {
						throw new RuntimeException("Move does not found");
					}
					if(!usedMoves.contains(move)) {
						usedMoves.add(move);
					}
					move.getStart().setNextMove(move);
					move.getEnd().setPreviousMove(move);
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					if(a.isEmpty()) {
						a.add(point);
					} else {
						TPoint tPoint = a.get(a.size() - 1);
						if(tPoint.getTime() + (SAMPLING_RATE * 1000) < record.getTime().getTime()) {
							a.add(point);
						}
					}
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					move.setAttribute(AttributeType.TRAJECTORY, s);
				}
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(String[] zones, Connection conn, Map<Integer, Stop> stops,
			Map<Integer, Move> moves) throws SQLException {
		String pointsTable = this.withTransportation ? "geolife.geolife_enriched_transportation_means" : "public.geolife2_limited";
		String transportationColumn = this.withTransportation ? "mode" : "transportation_mean";
		//
		transportationColumn = "'NONE'";
		pointsTable = "geolife.geolife_inside_university_5_pois_raw";
		//
		String sql = "select tid, gid, time, lon, lat, folder_id as user_id, " + transportationColumn  + " as transportationMode, \"POI\", semantic_stop_id, semantic_move_id, path, direction "
				+ "from " + pointsTable//
				+ " where 1=1 ";//
		if(zones != null && zones.length > 0) {
			sql += "and POI in (SELECT * FROM unnest(?)) ";
		}
		//
//		sql += "and tid in (14255,17936,23500) ";
		sql += "and (direction, path) in (select direction, path from "+ pointsTable + " group by direction, path having count(distinct tid) > 3) ";
		//
		sql += "order by tid, time, gid";
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(zones != null && zones.length > 0) {
			Array array = conn.createArrayOf("varchar", zones);
			preparedStatement.setArray(1, array);
		}
		ResultSet data = preparedStatement.executeQuery();
		Multimap<Integer, GeolifeRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
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
			GeolifeRecord record = new GeolifeRecord(
					data.getInt("tid"),
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getDouble("lon"),
				data.getDouble("lat"),
				data.getInt("user_id"),
				data.getString("transportationMode"),
				data.getString("POI"),
				data.getString("path"),
				data.getString("direction"),
				stop,
				move
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
				if(i > 0) {
					TPoint p = Semantic.SPATIAL.getData(s, i - 1);
					if(p.getTime() + (SAMPLING_RATE * 1000) > record.getTime().getTime()) {
						continue;
					}
				}
				s.addData(i, Semantic.GID, record.getGid());
				TPoint point = new TPoint(record.getLatitude(), record.getLongitude(), record.getTime());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, USER_ID, record.getUserId());
				s.addData(i, TRANSPORTATION_MODE, record.getTransportationMode());
				s.addData(i, REGION_INTEREST, record.getPOI());
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, PATH, record.getPath());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
					stop.setRegion(record.getPOI());
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
			double distance = Distance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION);
//			System.out.println(move.toString() + " = " + distance);
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, distance);
		}
	}
}
