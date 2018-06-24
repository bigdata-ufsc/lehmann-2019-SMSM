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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesProblem;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;

public class FastCBSMoT_Involves {

	private DataSource source;
	private static String YEAR_MONTH = "_com_auditoria";

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// FIND STOPS
		//double ratio = 200; // distance in meters to find neighbors
		//int stopMinutes = 30;
		//double maxDist = 200; // distance in meters to merge stops
		int toleranceMinutes = 2;
		FastCBSMoT_Involves fastCBSMoT_Involves = new FastCBSMoT_Involves();
		for (int ratio = 100; ratio <= 300; ratio+=100) {
			for (int stopMinutes = 15; stopMinutes <= 60; stopMinutes+=15) {
				int maxDist = ratio;
				fastCBSMoT_Involves.fastCBSMoT((int) ratio, stopMinutes, (int) maxDist, toleranceMinutes);
			}
		}
	}

	private void fastCBSMoT(int ratio, int stopMinutes, int maxDist, int toleranceMinutes)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		int timeTolerance = stopMinutes * 60 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		int mergeTolerance = toleranceMinutes * 60 * 60 * 1000;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = stopMinutes * 60  * 1000; // time in ms
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.amsterdan_stop", null, "geom");
		
		prepareDatabase(source, ratio, stopMinutes, maxDist, toleranceMinutes);
		
		
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction(), new StopAndMoveExtractor.PropertiesCallback() {
			
			@Override
			public void addProperties(SemanticTrajectory traj, Stop stop) {
				stop.setUser(InvolvesDatabaseReader.USER_ID.getData(traj, 0));
			}
			
			@Override
			public void addProperties(SemanticTrajectory traj, Move move) {
				move.setUser(InvolvesDatabaseReader.USER_ID.getData(traj, 0));
			}
		});
		InvolvesProblem problem = new InvolvesProblem(false, YEAR_MONTH, YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins");
		List<SemanticTrajectory> trajs = problem.data();

		// Trajectory t = retriever.fastFetchTrajectory(9543);

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(id) from involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\"");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(id) from involves.\"moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\"");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
//		PreparedStatement update = conn.prepareStatement("update public.amsterdan_park_cbsmot set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
//		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.amsterdan_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
//		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.amsterdan_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		
		PreparedStatement insertStop = conn.prepareStatement("insert into involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\"(id, start_timestamp, end_timestamp, longitude, latitude, \"closest_PDV\", \"PDV_distance\", \"closest_colab_PDV\", \"colab_PDV_distance\", is_home, id_colaborador_unidade) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into involves.\"moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\"(id, start_timestamp, end_timestamp, id_colaborador_unidade) values (?,?,?,?)");
		PreparedStatement insertMapping = conn.prepareStatement("insert into involves.\"stops_moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\"(gps_point_id, is_stop, is_move, semantic_id) values (?,?,?,?)");

		List<PDV> pdvs = new ArrayList<>();
		PreparedStatement pdvsPS = conn.prepareStatement("select id_ponto_de_venda_unidade, nome, bairro, endereco, numero, complemento, st_x(st_transform(st_setsrid(st_makepoint(longitude, latitude), 4326), 900913)) as lat, st_y(st_transform(st_setsrid(st_makepoint(longitude, latitude), 4326), 900913)) as lon from involves.pontos_venda");
		ResultSet pdvsR = pdvsPS.executeQuery();
		while(pdvsR.next()) {
			pdvs.add(new PDV(pdvsR.getInt("id_ponto_de_venda_unidade"), pdvsR.getDouble("lon"), pdvsR.getDouble("lat")));
		}
		pdvsPS.close();
		List<PDV> houses = new ArrayList<>();
		PreparedStatement housesPS = conn.prepareStatement("SELECT id_colaborador_unidade, st_x(st_transform(st_setsrid(st_makepoint(longitude, latitude), 4326), 900913)) as lat, st_y(st_transform(st_setsrid(st_makepoint(longitude, latitude), 4326), 900913)) as lon	FROM involves.colaboradores;");
		ResultSet pdvsH = housesPS.executeQuery();
		while(pdvsH.next()) {
			houses.add(new PDV(true, pdvsH.getInt("id_colaborador_unidade"), pdvsH.getDouble("lon"), pdvsH.getDouble("lat")));
		}
		housesPS.close();

		Multimap<Integer, PDV> pdvsPerUser = MultimapBuilder.hashKeys().arrayListValues().build();
		PreparedStatement pdvsPerUserPS = conn.prepareStatement("select id_ponto_de_venda_unidade, id_colaborador_unidade from involves.auditoria");
		ResultSet pdvsPerUserRS = pdvsPerUserPS.executeQuery();
		while(pdvsPerUserRS.next()) {
			pdvsPerUser.put(pdvsPerUserRS.getInt("id_colaborador_unidade"), new PDV(pdvsPerUserRS.getInt("id_ponto_de_venda_unidade"), 0, 0));
		}
		pdvsPerUserPS.close();
		
//		AtomicInteger sid = new AtomicInteger(0);
//		AtomicInteger mid = new AtomicInteger(0);
		try {
			conn.setAutoCommit(false);
			List<StopAndMove> findBestCBSMoT = StopAndMoveExtractor.findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), ratio, timeTolerance, maxDist, mergeTolerance, minTime, sid, mid);
			int stopsCount = 0;
			for (StopAndMove stopAndMove : findBestCBSMoT) {
				List<Stop> stops = stopAndMove.getStops();
				stopsCount += stops.size();
				for (Stop stop : stops) {
					int stopId = sid.incrementAndGet();
					List<TPoint> gpsPoints = stop.getPoints();
					for (TPoint tPoint : gpsPoints) {
						insertMapping.setLong(1, tPoint.getGid());
						insertMapping.setBoolean(toleranceMinutes, true);
						insertMapping.setBoolean(3, false);
						insertMapping.setLong(4, stopId);
						insertMapping.addBatch();
					}
					insertMapping.executeBatch();

					TPoint meanPoint = stop.medianPoint();
//					System.out.println("Median point: " + meanPoint.getGid());
					insertStop.setInt(1, stopId);
					insertStop.setTimestamp(toleranceMinutes, Timestamp.from(Instant.ofEpochMilli(stop.getStartTime())));
					insertStop.setTimestamp(3, Timestamp.from(Instant.ofEpochMilli(stop.getEndTime())));
					insertStop.setDouble(4, meanPoint.getX());
					insertStop.setDouble(5, meanPoint.getY());
					PDV closestPdv = closestPDV(pdvs, houses, null, meanPoint.getX(), meanPoint.getY(), stop.getUser());
					PDV closestUserPdv = closestPDV(pdvs, houses, pdvsPerUser, meanPoint.getX(), meanPoint.getY(), stop.getUser());
					insertStop.setInt(6, closestPdv.getId());
					insertStop.setDouble(7, closestPdv.distanceFrom(meanPoint.getX(), meanPoint.getY()));
					insertStop.setInt(8, closestUserPdv.getId());
					insertStop.setDouble(9, closestUserPdv.distanceFrom(meanPoint.getX(), meanPoint.getY()));
					insertStop.setBoolean(10, closestPdv.isHome(stop.getUser()));
					insertStop.setInt(11, stop.getUser());
					insertStop.execute();
				}
				List<Move> moves = stopAndMove.getMoves();
				for (Move move : moves) {
					TPoint[] gpsPoints = move.getPoints();
					int moveId = mid.incrementAndGet();
					if(gpsPoints != null) {
						for (TPoint tPoint : gpsPoints) {
							insertMapping.setLong(1, tPoint.getGid());
							insertMapping.setBoolean(toleranceMinutes, false);
							insertMapping.setBoolean(3, true);
							insertMapping.setLong(4, moveId);
							insertMapping.addBatch();
						}
						insertMapping.executeBatch();
					}
					insertMove.setInt(1, moveId);
					insertMove.setTimestamp(toleranceMinutes, Timestamp.from(Instant.ofEpochMilli(move.getStartTime())));
					insertMove.setTimestamp(3, Timestamp.from(Instant.ofEpochMilli(move.getEndTime())));
					insertMove.setInt(4, move.getUser());
					insertMove.execute();
				}
				conn.commit();
			}
			System.out.println("Stop count: " + stopsCount);
			
//			StopAndMoveExtractor.extractStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insertStop, insertMove);
		} finally {
//			update.close();
//			insertStop.close();
//			insertMove.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}
	
	private void prepareDatabase(DataSource source, int ratio, int stopMinutes, int maxDist, int toleranceMinutes) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = source.getRetriever().getConnection();

		try {
			conn.createStatement().execute("DROP VIEW involves.\"vw_agendada_vs_realizada_CBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		try {
			conn.createStatement().execute("DROP VIEW involves.\"vw_realizada_CBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		try {
			conn.createStatement().execute("DROP TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		
		conn.createStatement().execute("CREATE TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"(\n" + 
				"    id bigint NOT NULL,\n" + 
				"    start_timestamp timestamp(6) without time zone,\n" + 
				"    end_timestamp timestamp without time zone,\n" + 
				"    longitude double precision,\n" + 
				"    latitude double precision,\n" + 
				"    \"closest_PDV\" integer,\n" + 
				"    \"PDV_distance\" double precision,\n" + 
				"    is_home boolean,\n" + 
				"    id_colaborador_unidade integer,\n" + 
				"    \"closest_colab_PDV\" integer,\n" + 
				"    \"colab_PDV_distance\" double precision\n" + 
				")\n" + 
				"WITH (\n" + 
				"    OIDS = FALSE\n" + 
				")\n" + 
				"TABLESPACE data_trajectories;\n" + 
				"\n" + 
				"ALTER TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"    OWNER to postgres;");

		try {
			conn.createStatement().execute("DROP TABLE involves.\"moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		
		conn.createStatement().execute("\n" + 
				"CREATE TABLE involves.\"moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"(\n" + 
				"    id bigint,\n" + 
				"    id_colaborador_unidade bigint,\n" + 
				"    start_timestamp timestamp without time zone,\n" + 
				"    end_timestamp timestamp without time zone\n" + 
				")" +
				"WITH (\n" + 
				"    OIDS = FALSE\n" + 
				")\n" + 
				"TABLESPACE data_trajectories;\n" + 
				"\n" + 
				"ALTER TABLE involves.\"moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"    OWNER to postgres;");

		try {
			conn.createStatement().execute("DROP TABLE involves.\"stops_moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		
		conn.createStatement().execute("\n" + 
				"CREATE TABLE involves.\"stops_moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"(\n" + 
				"    gps_point_id bigint NOT NULL,\n" + 
				"    is_stop boolean,\n" + 
				"    is_move boolean,\n" + 
				"    semantic_id integer\n" + 
				")" +
				"WITH (\n" + 
				"    OIDS = FALSE\n" + 
				")\n" + 
				"TABLESPACE data_trajectories;\n" + 
				"\n" + 
				"ALTER TABLE involves.\"stops_moves_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\"\n" + 
				"    OWNER to postgres;");

		conn.createStatement().execute("\n" + 
				"CREATE OR REPLACE FUNCTION involves.array_uniq_stable(\n" + 
				"	anyarray)\n" + 
				"    RETURNS anyarray\n" + 
				"    LANGUAGE 'sql'\n" + 
				"AS $BODY$\n" + 
				"\n" + 
				"SELECT\n" + 
				"    array_agg(distinct_value ORDER BY first_index)\n" + 
				"FROM \n" + 
				"    (SELECT\n" + 
				"        value AS distinct_value, \n" + 
				"        min(index) AS first_index \n" + 
				"    FROM \n" + 
				"        unnest($1) WITH ORDINALITY AS input(value, index)\n" + 
				"    GROUP BY\n" + 
				"        value\n" + 
				"    ) AS unique_input\n" + 
				";\n" + 
				"\n" + 
				"$BODY$;");
		
		conn.createStatement().execute("\n" + 
				"CREATE OR REPLACE VIEW involves.\"vw_realizada_CBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\" AS\n" + 
				" SELECT gps_1.id_usuario,\n" + 
				"    gps_1.id_dimensao_data,\n" + 
				"    array_to_string(involves.array_uniq_stable(array_agg(aud_1.\"closest_colab_PDV\")), ','::text) AS pdvs_visitados\n" + 
				"   FROM involves.dados_colaboradores gps_1\n" + 
				"     JOIN involves.dimensao_data dt ON gps_1.id_dimensao_data = dt.id\n" + 
				"     JOIN involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\" aud_1 ON gps_1.id_usuario = aud_1.id_colaborador_unidade AND aud_1.start_timestamp >= to_date((((dt.dia || '/'::text) || dt.mes) || '/'::text) || dt.ano, 'DD/MM/YYYY'::text) AND aud_1.start_timestamp <= (to_date((((dt.dia || '/'::text) || dt.mes) || '/'::text) || dt.ano, 'DD/MM/YYYY'::text) + 1)\n" + 
				"     JOIN involves.auditoria aud ON aud.id_dimensao_data = dt.id::numeric AND gps_1.id_usuario::numeric = aud.id_colaborador_unidade\n" + 
				"  WHERE aud_1.is_home = false AND gps_1.provedor::text = 'gps'::text AND gps_1.dt_coordenada >= aud_1.start_timestamp AND gps_1.dt_coordenada <= aud_1.end_timestamp\n" + 
				"  GROUP BY gps_1.id_usuario, gps_1.id_dimensao_data;\n" + 
				"");
		
		conn.createStatement().execute("CREATE OR REPLACE VIEW involves.\"vw_agendada_vs_realizada_CBSMoT"  + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\" AS\n" +
				" SELECT aud.id_colaborador_unidade,\n" + 
				"    aud.id_dimensao_data,\n" + 
				"    aud.pdvs_agendados,\n" + 
				"    gps.pdvs_visitados\n" + 
				"   FROM ( SELECT aud_1.id_colaborador_unidade,\n" + 
				"            aud_1.id_dimensao_data,\n" + 
				"            string_agg(DISTINCT (aud_1.ordem_prevista::text || ' - '::text) || aud_1.id_ponto_de_venda_unidade::text, ','::text) AS pdvs_agendados\n" + 
				"           FROM ( SELECT aud_2.id_colaborador_unidade,\n" + 
				"                    aud_2.id_ponto_de_venda_unidade,\n" + 
				"                    aud_2.id_dimensao_data,\n" + 
				"                    aud_2.ordem_prevista\n" + 
				"                   FROM involves.auditoria aud_2\n" + 
				"                  ORDER BY aud_2.id_colaborador_unidade, aud_2.ordem_prevista) aud_1\n" + 
				"          GROUP BY aud_1.id_colaborador_unidade, aud_1.id_dimensao_data) aud\n" + 
				"     LEFT JOIN ( SELECT realizada.id_usuario,\n" + 
				"            realizada.id_dimensao_data,\n" + 
				"            realizada.pdvs_visitados\n" + 
				"           FROM involves.\"vw_realizada_CBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins\" realizada) gps ON gps.id_usuario::numeric = aud.id_colaborador_unidade AND gps.id_dimensao_data::numeric = aud.id_dimensao_data;\n" + 
				"");
	}

	private static PDV closestPDV(List<PDV> allPDVs, List<PDV> houses, Multimap<Integer, PDV> pdvsPerUser, double x, double y, Integer userId) {
		Point p2 = new Point(x, y);
		double minDist = Double.POSITIVE_INFINITY;
		PDV minPDV = null;
		if(pdvsPerUser != null) {
			Collection<PDV> filtered = pdvsPerUser.get(userId);
			allPDVs = allPDVs.stream().filter(pdv -> {
				return filtered.contains(pdv);
			}).collect(Collectors.toList());
		}
		for (PDV pdv : allPDVs) {
			double dist = Distance.euclidean(new Point(pdv.latitude, pdv.longitude), p2);
			if(minDist > dist) {
				minPDV = pdv;
				minDist = dist;
			}
		}
		for (PDV pdv : houses) {
			if(pdv.userId == userId) {
				double dist = Distance.euclidean(new Point(pdv.latitude, pdv.longitude), p2);
				if(minDist > dist) {
					minPDV = pdv;
					minDist = dist;
				}
			}
		}
		return minPDV;
	}
	
	private static class PDV {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + pdvId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PDV other = (PDV) obj;
			if (pdvId != other.pdvId)
				return false;
			return true;
		}

		private int pdvId = -1;
		private double longitude;
		private double latitude;
		private boolean isHome;
		private int userId = -1;

		public PDV(int pdvId, double longitude, double latitude) {
			this.pdvId = pdvId;
			this.longitude = longitude;
			this.latitude = latitude;
		}

		public PDV(boolean isHome, int userId, double longitude, double latitude) {
			this.isHome = isHome;
			this.userId = userId;
			this.longitude = longitude;
			this.latitude = latitude;
		}

		public int getId() {
			return pdvId;
		}

		public boolean isHome(int user) {
			return isHome && this.userId == user;
		}

		public double distanceFrom(double x, double y) {
			return Distance.euclidean(new Point(this.latitude, this.longitude), new Point(x, y));
		}
		
	}

}
