package br.ufsc.lehmann;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class GeolifePathCleaner {

	public static void main(String[] args) {
		GeolifeUniversitySubProblem problem = new GeolifeUniversitySubProblem(GeolifeUniversityDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, true);
		Map<SemanticTrajectory, MutablePair<Integer, Integer>> newBounds = new HashMap<>();
		List<SemanticTrajectory> data = problem.data();
		for (SemanticTrajectory t : data) {
			Set<Long> gidToDelete = new LinkedHashSet<>();
			Set<String> pois = new HashSet<>();
			String caminho = null;
			if(GeolifeUniversityDataReader.DIRECTION.getData(t, 0).equals("Microsoft-Dormitory")) {
				caminho = "Microsoft-Dormitory";
			}
			if(GeolifeUniversityDataReader.DIRECTION.getData(t, 0).equals("Dormitory-Microsoft")) {
				caminho = "Dormitory-Microsoft";
			}
			Stop previousStop = null;
			for (int i = 0; i < t.length(); i++) {
				Stop s = GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC.getData(t, i);
				if(s != null && pois.isEmpty()) {
					if(caminho.equals("Microsoft-Dormitory") && !s.getStopName().equals("Microsoft")) {
						
						int j = i;
						for (; j < t.length(); j++) {
							Stop nextStop = GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC.getData(t, j);
							if(nextStop != null && nextStop.getStopName().equals("Microsoft")) {
								newBounds.computeIfAbsent(t, (x) -> new MutablePair<>()).setLeft(nextStop.getBegin());
								break;
							}
						}
						i = j - 1;
						continue;
					} else if(caminho.equals("Dormitory-Microsoft") && !s.getStopName().equals("Home")) {
						int j = i;
						for (; j < t.length(); j++) {
							Stop nextStop = GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC.getData(t, j);
							if(nextStop != null && nextStop.getStopName().equals("Home")) {
								newBounds.computeIfAbsent(t, (x) -> new MutablePair<>()).setLeft(nextStop.getBegin());
								break;
							}
						}
						i = j - 1;
						continue;
					}
					pois.add(s.getStopName());
				} else if (previousStop != null) {
					gidToDelete.remove(gidToDelete.size() - 1);
				}
				previousStop = s;
			}
			System.out.println(gidToDelete);
			break;
		}
	}
}
