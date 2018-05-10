package br.ufsc.lehmann;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class GeolifeStopMerger {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		GeolifeUniversitySubProblem problem = new GeolifeUniversitySubProblem(GeolifeUniversityDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, false);
		List<SemanticTrajectory> data = problem.data();
		Set<Long> gidToDelete = new LinkedHashSet<>();
		for (SemanticTrajectory t : data) {
			Map<String, Stop> pois = new HashMap<>();
			Stop previousStop = null;
			for (int i = 0; i < t.length(); i++) {
				Stop s = GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC.getData(t, i);
				if(s != null) {
					String stopName = (String) s.getRegion();
					if(pois.containsKey(stopName)) {
						if(pois.size() == 1) {
							//remove todos os pontos anteriores
							for (int j = s.getBegin() + s.getLength() - 1; j > 0; j--) {
								gidToDelete.add(Semantic.GID.getData(t, j - 1).longValue());
							}
							previousStop = s;
						} else {
							if(stopName.equals(previousStop.getRegion()) && s != previousStop) {
								for (int j = s.getBegin() + s.getLength() - 1; j > previousStop.getBegin() + 1; j--) {
									gidToDelete.add(Semantic.GID.getData(t, j - 1).longValue());
								}
							} else {
								previousStop = s;
							}
						}
					}
					pois.put(stopName, s);
					previousStop = s;
				}
			}
		}
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "geolife.geolife_with_pois_university", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);

		PreparedStatement ps = conn.prepareStatement("delete from geolife.geolife_with_pois_university where gid in (SELECT * FROM unnest(?))");

		Array array = conn.createArrayOf("integer", gidToDelete.toArray(new Long[gidToDelete.size()]));
		ps.setArray(1, array);
		
		ps.execute();
		conn.commit();
	}
}
