package br.ufsc.lehmann.stopandmove;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class FastCBSMoT_BusesPatel {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction());
		PatelProblem problem = new PatelProblem("buses");
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicles", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 300; // distance in meters to find neighbors
		int timeTolerance = 28; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Ser� feito merge nos stops em que a dist�ncia dos centroid estiver h� at� maxDist de dist�ncia
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 350; // distance in meters to merge stops
		int mergeTolerance = 200;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 20; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();
		
		ResultSet executeQuery = conn.createStatement().executeQuery("select max(stop_id), max(move_id) from stops_moves.patel_vehicles");
		executeQuery.next();
		MutableInt sid = new MutableInt(executeQuery.getInt(1));
		MutableInt mid = new MutableInt(executeQuery.getInt(2));
		PreparedStatement update = conn.prepareStatement("update patel.buses set semantic_stop_id = ?, semantic_move_id = ? where gid in (SELECT * FROM unnest(?))");
		PreparedStatement insert = conn.prepareStatement("insert into stops_moves.patel_vehicles(stop_id, start_time, start_lat, start_lon, end_time, end_lat, end_lon, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			Map<String, Integer> bestCombinations = StopAndMoveExtractor.findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
				if(e.getValue() > 400){
					System.out.println(e.getKey() + " ->" + e.getValue());
				}
			}
			StopAndMoveExtractor.persistStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insert);
		} finally {
			update.close();
			insert.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}
}
