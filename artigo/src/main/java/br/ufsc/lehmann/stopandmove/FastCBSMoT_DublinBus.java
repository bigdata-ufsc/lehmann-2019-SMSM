package br.ufsc.lehmann.stopandmove;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;

public class FastCBSMoT_DublinBus {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DublinBusProblem problem = new DublinBusProblem();
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops&moves.dublin_201301", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 50; // distance in meters to find neighbors
		int timeTolerance = 30 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 200; // distance in meters to merge stops
		int mergeTolerance = 2 * 60 * 1000;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 45 * 1000; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();
		
		ResultSet executeQuery = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.bus_dublin_201301");
		executeQuery.next();
		MutableInt sid = new MutableInt(executeQuery.getInt(1));
		PreparedStatement update = conn.prepareStatement("update bus.dublin_201301 set semantic_stop_id = ?, semantic_move_id = ? where gid in (SELECT * FROM unnest(?))");
		PreparedStatement insert = conn.prepareStatement("insert into stops_moves.bus_dublin_201301(stop_id, start_time, start_lat, start_lon, end_time, end_lat, end_lon, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			while (!trajs.isEmpty()) {
				SemanticTrajectory t = trajs.remove(0);
				StopAndMove stopAndMove = FastCBSMoT.findStops(t, maxDist, minTime, timeTolerance, mergeTolerance, ratio, sid);
				List<Stop> stops = stopAndMove.getStops();
				if(stops.size() > 0) {
					System.out.println("Traj=" + t.getTrajectoryId() + ", stops=" + stops.size());
					for (Stop stop : stops) {
//						System.out.println("From " + stop.getStartTime() + " to " + stop.getEndTime());
						List<Integer> gids = stopAndMove.getGids(stop);
						Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
						update.setInt(1, stop.getStopId());
						update.setNull(2, Types.NUMERIC);
						update.setArray(3, array);
						update.executeUpdate();
						
						List<TPoint> points = new ArrayList<>(stop.getPoints());
						insert.setInt(1, stop.getStopId());
						insert.setTimestamp(2, stop.getStartTime());
						insert.setDouble(3, points.get(0).getX());
						insert.setDouble(4, points.get(0).getY());
						insert.setTimestamp(5, stop.getEndTime());
						insert.setDouble(6, points.get(points.size() - 1).getX());
						insert.setDouble(7, points.get(points.size() - 1).getY());
						insert.setDouble(8, stop.getCentroid().getX());
						insert.setDouble(9, stop.getCentroid().getY());
						insert.executeUpdate();
					}
					conn.commit();
				}
			}
		} finally {
			update.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

}
