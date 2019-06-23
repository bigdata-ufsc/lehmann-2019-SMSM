package br.ufsc.lehmann;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.related.DTW;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.lehmann.method.EDwP;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix.Tuple;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class TesteNovaIorque {

	public static void main(String[] args) throws IOException, ParseException {
		NewYorkBusDataReader reader = new NewYorkBusDataReader(false, StopMoveStrategy.SMoT);
		List<SemanticTrajectory> trajs = reader.read(new String[] {"MTA NYCT_M4", "MTA NYCT_M3", "MTA NYCT_M2", "MTA NYCT_M1"});
		List<Tuple<SemanticTrajectory, Double>> ret = new ArrayList<>(trajs.size());
		trajs.stream().forEach(t -> ret.add(new Tuple<SemanticTrajectory, Double>(t, null)));
		SemanticTrajectory semanticTrajectory = trajs.get(0);
		System.out.println("Selected traj: " + semanticTrajectory.getTrajectoryId());
		DTW<TPoint, Number> ums = new DTW<>(Semantic.SPATIAL_EUCLIDEAN);
		for (int i = 0; i < trajs.size(); i++) {
			ret.get(i).setLast(ums.distance(semanticTrajectory, trajs.get(i)));
		}
//		UMS ums = new UMS();
//		for (int i = 0; i < trajs.size(); i++) {
//			ret.get(i).setLast(ums.distance(semanticTrajectory, trajs.get(i)));
//		}
//		EDwP ums = new EDwP(false);
//		for (int i = 0; i < trajs.size(); i++) {
//			ret.get(i).setLast(ums.getDistance(semanticTrajectory, trajs.get(i)));
//		}
		Collections.sort(ret, new Comparator<Tuple<SemanticTrajectory, Double>>() {

			@Override
			public int compare(Tuple<SemanticTrajectory, Double> o1, Tuple<SemanticTrajectory, Double> o2) {
				return Double.compare(o1.getLast(), o2.getLast());
			}
		});
		for (int i = 1; i < trajs.size(); i++) {
			System.out.printf("\t %s - %.4f\n", ret.get(i).getFirst().getTrajectoryId(), ret.get(i).getLast());
		}
	}
}
