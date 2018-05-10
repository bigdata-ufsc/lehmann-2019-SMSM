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
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_Regions_Problem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class SanFranciscoStopMerger {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		SanFranciscoCab_Regions_Problem problem = new SanFranciscoCab_Regions_Problem(SanFranciscoCabDatabaseReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}, new String[] {"mall", "intersection_101_280", "bayshore_fwy", "airport"}, false);
		List<SemanticTrajectory> data = problem.data();
		Set<Long> gidToDelete = new LinkedHashSet<>();
		for (SemanticTrajectory t : data) {
			Map<String, Stop> pois = new HashMap<>();
//			if(!t.getTrajectoryId().equals(176078)) {
//				continue;
//			}
			Stop previousStop = null;
			for (int i = 0; i < t.length(); i++) {
				Stop s = SanFranciscoCabDatabaseReader.STOP_REGION_SEMANTIC.getData(t, i);
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
//			if(!gidToDelete.isEmpty()) {
//				System.out.println(t.getTrajectoryId() + ": " + gidToDelete);
//			}
		}
		System.out.println(gidToDelete);
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.sanfrancisco_taxicab", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);

		PreparedStatement ps = conn.prepareStatement("delete from taxi.sanfrancisco_taxicab where gid in (SELECT * FROM unnest(?))");

		Array array = conn.createArrayOf("integer", gidToDelete.toArray(new Long[gidToDelete.size()]));
		ps.setArray(1, array);
		
		ps.execute();
		conn.commit();
	}
}
