package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;

public class SMoT_Geolife_University {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		GeolifeProblem problem = new GeolifeUniversitySubProblem();
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.geolife_university_stop", null, "geom");

		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.geolife_university_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.geolife_university_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update geolife.geolife_university set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.geolife_university_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon, \"POI\") values (?,?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.geolife_university_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			FastSMoT<String, Number> fastSMoT = new FastSMoT<>(GeolifeDatabaseReader.REGION_INTEREST, 0);
			List<StopAndMove> bestSMoT = new ArrayList<>();
			for (SemanticTrajectory T : trajs) {
				bestSMoT.add(fastSMoT.findStops(T, sid, mid));
			}
			int stopCount = 0;
			for (StopAndMove stopAndMove : bestSMoT) {
				stopCount += stopAndMove.getStops().size();
			}
			System.out.println("Stops found: " + stopCount);
			StopAndMoveExtractor.persistStopAndMove(conn, update, insertStop, new StopAndMoveExtractor.StopPersisterCallback() {

				@Override
				public void parameterize(PreparedStatement statement, Stop stop) throws SQLException {
					//Home - POLYGON ((12948247.973819174 4866575.90590961, 12948247.973819174 4867187.41762101, 12948905.637357853 4867187.41762101, 12948905.637357853 4866575.90590961, 12948247.973819174 4866575.90590961))
					//Park POLYGON ((12949234.988222102 4865626.93591852, 12949234.988222102 4866053.46494659, 12949689.747565899 4866053.46494659, 12949689.747565899 4865626.93591852, 12949234.988222102 4865626.93591852))
					//Starbucks POLYGON ((1.2949129492226938E7 4864510.61163902,1.2949129492226938E7 4865155.72337913,1.2949421936024217E7 4865155.72337913,1.2949421936024217E7 4864510.61163902,1.2949129492226938E7 4864510.61163902))
					//Market POLYGON ((1.2949421625986239E7 4864510.64276728,1.2949421625986239E7 4865156.95972689,1.2949756605852714E7 4865156.95972689,1.2949756605852714E7 4864510.64276728,1.2949421625986239E7 4864510.64276728))
					//Microsoft POLYGON ((12949367.15563061 4862122.2545775, 12949367.15563061 4862756.84220254, 12950047.895082928 4862756.84220254, 12950047.895082928 4862122.2545775, 12949367.15563061 4862122.2545775))
					statement.setString(12, String.valueOf(stop.getStopName()));
					double lat, lon;
					if(stop.getStopName().equals("Home")) {
						//'4866881.66176531','12948576.8055885'
						lat = 4866881.66176531;
						lon = 12948576.8055885;
					} else if(stop.getStopName().equals("Park")) {
						//'4865840.20043256','12949462.367894'
						lat = 4865840.20043256;
						lon = 12949462.367894;
					} else if(stop.getStopName().equals("Starbucks")) {
						//POINT(12949275.7141256 4864833.16750908)
						lat = 4864833.16750908;
						lon = 12949275.7141256;
					} else if(stop.getStopName().equals("Market")) {
						//POINT(12949589.1159195 4864833.80124708)
						lat = 4864833.80124708;
						lon = 12949589.1159195;
					} else if(stop.getStopName().equals("Work")) {
						//'4862439.54839002','12949707.5253568'
						lat = 4862439.54839002;
						lon = 12949707.5253568;
					} else {
						lat = -1;
						lon = -1;
					}
					statement.setDouble(11, lat);//lat
					statement.setDouble(10, lon);//lon
				}
				
			}, insertMove, bestSMoT);
						
		} finally {
			update.close();
			insertStop.close();
			insertMove.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

}
