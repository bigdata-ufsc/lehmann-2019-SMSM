package br.ufsc.lehmann;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class GeolifeStopPointsCleaner {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		GeolifeUniversitySubProblem problem = new GeolifeUniversitySubProblem(GeolifeUniversityDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, false);
		List<SemanticTrajectory> data = problem.data();
		List<Long> gidToDelete = new ArrayList<>();
		for (SemanticTrajectory t : data) {
			Stop previousStop = null;
			for (int i = 0; i < t.length(); i++) {
				Stop s = GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC.getData(t, i);
				if(s != null) {
					if(s == previousStop) {
						gidToDelete.add(Semantic.GID.getData(t, i).longValue());
					}
				} else if (previousStop != null) {
					gidToDelete.remove(gidToDelete.size() - 1);
				}
				previousStop = s;
			}
		}
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.geolife_with_pois_university", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);

		PreparedStatement ps = conn.prepareStatement("delete from public.geolife_with_pois_university where gid in (SELECT * FROM unnest(?))");

		Array array = conn.createArrayOf("integer", gidToDelete.toArray(new Long[gidToDelete.size()]));
		ps.setArray(1, array);
		
		ps.execute();
		conn.commit();
		
	}
}
