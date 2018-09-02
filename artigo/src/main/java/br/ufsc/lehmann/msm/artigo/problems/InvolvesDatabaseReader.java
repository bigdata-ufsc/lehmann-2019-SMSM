package br.ufsc.lehmann.msm.artigo.problems;

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

import org.apache.commons.collections4.CollectionUtils;
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
import br.ufsc.utils.Angle;
import br.ufsc.utils.Distance;
import br.ufsc.utils.LatLongDistanceFunction;

public class InvolvesDatabaseReader implements IDataReader {
	
	private static final LatLongDistanceFunction DISTANCE_FUNCTION = new LatLongDistanceFunction();
	public static final BasicSemantic<Integer> DIMENSAO_DATA = new BasicSemantic<>(3);
	public static final BasicSemantic<Integer> WEEK = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DAY_OF_WEEK = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> USER_ID = new BasicSemantic<>(6);
	public static final StopSemantic STOP_CENTROID_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, DISTANCE_FUNCTION));
	public static final StopSemantic STOP_STREET_NAME_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_STREET_NAME, new EqualsDistanceFunction<String>()));
	public static final StopSemantic STOP_NAME_SEMANTIC = new StopSemantic(7, new AttributeDescriptor<Stop, String>(AttributeType.STOP_NAME, new EqualsDistanceFunction<String>()));
	
	public static final MoveSemantic MOVE_ANGLE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic MOVE_DISTANCE_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic MOVE_TEMPORAL_DURATION_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_DURATION, new NumberDistance()));
	public static final MoveSemantic MOVE_POINTS_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(DISTANCE_FUNCTION)));
	public static final MoveSemantic MOVE_ELLIPSES_SEMANTIC = new MoveSemantic(8, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance(DISTANCE_FUNCTION)));

	public static final BasicSemantic<String> TRAJECTORY_IDENTIFIER = new BasicSemantic<String>(9) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return USER_ID.getData(p, i) + "/" + DAY_OF_WEEK.getData(p, i);
		}
	};

	public static final BasicSemantic<String> WEEKLY_TRAJECTORY_IDENTIFIER = new BasicSemantic<String>(10) {
		@Override
		public String getData(SemanticTrajectory p, int i) {
			return USER_ID.getData(p, i) + "/" + WEEK.getData(p, i);
		}
	};
	
	public static final String SCHEMA = "colab1300";
	
	private boolean onlyStops;
	private String baseTable;
	private String stopMove_table;
	private boolean weekly;

	public InvolvesDatabaseReader(boolean onlyStops) {
		this(onlyStops, false, null, null);
	}

	public InvolvesDatabaseReader(boolean onlyStops, boolean weekly, String year_month, String stopMove_table) {
		this.onlyStops = onlyStops;
		this.weekly = weekly;
		this.baseTable = year_month == null ? "" : year_month;
		this.stopMove_table = stopMove_table == null ? "" : stopMove_table;
	}

	public List<SemanticTrajectory> read() {
		try {
			return internalRead();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private List<SemanticTrajectory> internalRead()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.amsterdan_park_cbsmot", null, null);
		DataRetriever retriever = source.getRetriever();
		Map<Integer, Stop> stops = new HashMap<>();
		Map<Integer, Move> moves = new HashMap<>();
		Multimap<String, InvolvesRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		Connection conn = retriever.getConnection();
		try {
			System.out.println("Executing SQL...");
			conn.setAutoCommit(false);
			Statement st = conn.createStatement();
			st.setFetchSize(1000);
	
			ResultSet stopsData = st.executeQuery(
					"SELECT id, start_timestamp, end_timestamp, start_lat, start_lon, end_lat, end_lon, begin, length, longitude, latitude, \"closest_PDV\", \"PDV_distance\", is_home, id_colaborador_unidade, \"closest_colab_PDV\" as closest_colab_pdv, \"colab_PDV_distance\"\r\n" + 
					"	FROM " + SCHEMA + ".\"stops_FastCBSMoT" + stopMove_table + "\";");
			while (stopsData.next()) {
				int stopId = stopsData.getInt("id");
				Stop stop = stops.get(stopId);
				if (stop == null) {
					stop = new Stop(stopId, 
							stopsData.getString("closest_colab_pdv"), //
							stopsData.getTimestamp("start_timestamp").getTime(), //
							stopsData.getTimestamp("end_timestamp").getTime(), //
							new TPoint(stopsData.getDouble("start_lon"), stopsData.getDouble("start_lat")), //
							stopsData.getInt("begin"), //
							new TPoint(stopsData.getDouble("end_lon"), stopsData.getDouble("end_lat")), //
							stopsData.getInt("length"), //
							new TPoint(stopsData.getDouble("longitude"), stopsData.getDouble("latitude")), //
							null, //
							null//
					);
					stops.put(stopId, stop);
				}
			}
			stopsData.close();
			ResultSet movesData = st
					.executeQuery("SELECT id, id_colaborador_unidade, start_timestamp, end_timestamp, start_stop_id, end_stop_id\r\n" + 
							"	FROM " + SCHEMA + ".\"moves_FastCBSMoT" + stopMove_table + "\"");
			while (movesData.next()) {
				int moveId = movesData.getInt("id");
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
					Stop startStop = stops.get(startStopId);
					Stop endStop = stops.get(endStopId);
					move = new Move(moveId, //
							startStop, //
							endStop, //
							movesData.getTimestamp("start_timestamp").getTime(), //
							movesData.getTimestamp("end_timestamp").getTime(), //
							-1, //
							-1, //
							null);
					move.setAttribute(AttributeType.MOVE_USER, movesData.getInt("id_colaborador_unidade"));
					moves.put(moveId, move);
					
				}
			}
			movesData.close();
			st.close();
	
			String sql = "select (gps.id_usuario::text || gps.id_dimensao_data::text || gps.id_dado_gps::text) as id_dado_gps, "//
					+ "gps.id_usuario, col.id_colaborador_unidade, gps.id_dimensao_data, dt.dia_semana, dt.semana, dt_coordenada, "//
					+ "st_x(st_transform(st_setsrid(st_makepoint(gps.longitude, gps.latitude), 4326), 900913)) as lon, "//
					+ "st_y(st_transform(st_setsrid(st_makepoint(gps.longitude, gps.latitude), 4326), 900913)) as lat, "
					+ "case when map.is_stop = true then map.semantic_id else null end as semantic_stop_id, "//
					+ "case when map.is_move = true then map.semantic_id else null end as semantic_move_id "//
					+ "from " + SCHEMA + ".\"dadoGps" + baseTable + "\" gps ";//
			sql += "inner join " + SCHEMA + ".dimensao_data dt on dt.id = gps.id_dimensao_data ";
			sql += "inner join " + SCHEMA + ".colaboradores col on col.id_usuario = gps.id_usuario ";
			sql += "left join " + SCHEMA + ".\"stops_moves_FastCBSMoT" + stopMove_table + "\" map on (gps.id_usuario::text || gps.id_dimensao_data::text || gps.id_dado_gps::text)::bigint = map.gps_point_id ";
			sql += "where provedor = 'gps' ";//
//			sql += "and gps.id_usuario= 1300 ";//
			sql += "order by gps.id_usuario, gps.id_dimensao_data, gps.dt_coordenada, gps.id_dado_gps";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			ResultSet data = preparedStatement.executeQuery();
			System.out.println("Fetching...");
			while(data.next()) {
				Integer stopId = data.getInt("semantic_stop_id");
				if(data.wasNull()) {
					stopId = null;
				}
				Integer moveId = data.getInt("semantic_move_id");
				if(data.wasNull()) {
					moveId = null;
				}
				InvolvesRecord record = new InvolvesRecord(
						data.getLong("id_dado_gps"),
						data.getInt("id_usuario"),
						data.getInt("id_colaborador_unidade"),
						data.getInt("id_dimensao_data"),
						data.getInt("semana"),
						data.getInt("dia_semana"),
						data.getTimestamp("dt_coordenada"),
						data.getDouble("lat"),
						data.getDouble("lon"),
						stopId,
						moveId
				);
				if(weekly) {
					records.put(record.getId_usuario() + "/" + record.getSemana(), record);
				} else {
					records.put(record.getId_usuario() + "/" + record.getId_dimensao_data(), record);
				}
			}
			data.close();
			preparedStatement.close();
		} finally {
			conn.close();
		}
		
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());

		List<SemanticTrajectory> ret = null;
		List<Move> usedMoves = new ArrayList<Move>();
		if(onlyStops) {
			ret = readStopsTrajectories(stops, moves, records, usedMoves);
		} else {
			ret = readRawPoints(stops, moves, records);
		}
		compute(usedMoves);
		return ret;
	}

	private List<SemanticTrajectory> readStopsTrajectories(Map<Integer, Stop> stops, Map<Integer, Move> moves, Multimap<String, InvolvesRecord> records, List<Move> usedMoves) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<InvolvesRecord> collection = records.get(trajId);
			int i = 0;
			Move currentMove = null;
			for (InvolvesRecord record : collection) {
				if(record.getSemanticStopId() == null && record.getSemanticMoveId() == null) {
					continue;
				}
				TPoint point = new TPoint(record.getId(), record.getLon(), record.getLat(), record.getDt_coordenada());
				if(record.getSemanticStopId() != null) {
					if(currentMove != null) {
						if(currentMove.getEnd() != null) {
							TPoint[] points = (TPoint[]) currentMove.getAttribute(AttributeType.MOVE_POINTS);
							List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
							a.add(currentMove.getEnd().getStartPoint());
							currentMove.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
						}
						currentMove = null;
					}
					Stop stop = stops.get(record.getSemanticStopId());
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
					s.addData(i, Semantic.GID, record.getId());
					s.addData(i, Semantic.SPATIAL_LATLON, stop.getCentroid());
					s.addData(i, USER_ID, record.getId_usuario());
					s.addData(i, DIMENSAO_DATA, record.getId_dimensao_data());
					s.addData(i, WEEK, record.getSemana());
					s.addData(i, DAY_OF_WEEK, record.getDiaSemana());
					s.addData(i, TRAJECTORY_IDENTIFIER, TRAJECTORY_IDENTIFIER.getData(s, i));
					s.addData(i, WEEKLY_TRAJECTORY_IDENTIFIER, WEEKLY_TRAJECTORY_IDENTIFIER.getData(s, i));
					i++;
				} else if(record.getSemanticMoveId() != null) {
					Move move = moves.remove(record.getSemanticMoveId());
					if(move == null) {
						for (int j = i - 1; j > -1; j--) {
							move = MOVE_ANGLE_SEMANTIC.getData(s, j);
							if(move != null) {
								break;
							}
						}
						if(move != null) {
							TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
							List<TPoint> a = new ArrayList<TPoint>(Arrays.asList(points));
							a.add(point);
							move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
							continue;
						}
					} else {
						usedMoves.add(move);
					}
					TPoint[] points = (TPoint[]) move.getAttribute(AttributeType.MOVE_POINTS);
					List<TPoint> a = new ArrayList<TPoint>(points == null ? Collections.emptyList() : Arrays.asList(points));
					a.add(point);
					move.setAttribute(AttributeType.MOVE_POINTS, a.toArray(new TPoint[a.size()]));
					s.addData(i, MOVE_ANGLE_SEMANTIC, move);
					s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(move.getStartTime()), Instant.ofEpochMilli(move.getEndTime())));
					s.addData(i, Semantic.SPATIAL, point);
				}
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}

	private List<SemanticTrajectory> readRawPoints(Map<Integer, Stop> stops, Map<Integer, Move> moves, Multimap<String, InvolvesRecord> records) {
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, 11);
			Collection<InvolvesRecord> collection = records.get(trajId);
			int i = 0;
			for (InvolvesRecord record : collection) {
				s.addData(i, Semantic.GID, record.getId());
				TPoint point = new TPoint(record.getId(), record.getLon(), record.getLat(), record.getDt_coordenada());
				s.addData(i, Semantic.SPATIAL, point);
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getDt_coordenada().getTime()), Instant.ofEpochMilli(record.getDt_coordenada().getTime())));
				s.addData(i, USER_ID, record.getId_usuario());
				s.addData(i, WEEK, record.getSemana());
				s.addData(i, DAY_OF_WEEK, record.getDiaSemana());
				s.addData(i, DIMENSAO_DATA, record.getId_dimensao_data());
				s.addData(i, TRAJECTORY_IDENTIFIER, TRAJECTORY_IDENTIFIER.getData(s, i));
				s.addData(i, WEEKLY_TRAJECTORY_IDENTIFIER, WEEKLY_TRAJECTORY_IDENTIFIER.getData(s, i));
				if(record.getSemanticStopId() != null) {
					Stop stop = stops.get(record.getSemanticStopId());
					s.addData(i, STOP_CENTROID_SEMANTIC, stop);
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
//			move.setAttribute(AttributeType.MOVE_ANGLE, Angle.getAngle(points.get(0), points.get(points.size() - 1)));
			move.setAttribute(AttributeType.MOVE_TRAVELLED_DISTANCE, Distance.getDistance(points.toArray(new TPoint[points.size()]), DISTANCE_FUNCTION));
		}
	}
}
