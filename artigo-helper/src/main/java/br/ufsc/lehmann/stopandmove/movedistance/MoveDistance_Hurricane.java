package br.ufsc.lehmann.stopandmove.movedistance;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.stopandmove.EuclideanDistanceFunction;

public class MoveDistance_Hurricane {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PatelProblem problem = new PatelProblem("hurricane");
		String moveTable = "stops_moves.patel_hurricane_move";
		List<SemanticTrajectory> trajs = problem.data();
		Map<Move, SemanticTrajectory> moves = new HashMap<>();
		for (SemanticTrajectory semanticTrajectory : trajs) {
			for (int j = 0; j < semanticTrajectory.length(); j++) {
				Move data = PatelDataReader.MOVE_ANGLE_SEMANTIC.getData(semanticTrajectory, j);
				if(data != null && !moves.containsKey(data)) {
					moves.put(data, semanticTrajectory);
				}
			}
		}
		new MoveDistance(new EuclideanDistanceFunction()).extractMovementTraveledDistance(moveTable, moves);
	}
}
