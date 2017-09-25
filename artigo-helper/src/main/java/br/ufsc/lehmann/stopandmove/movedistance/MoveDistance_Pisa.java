package br.ufsc.lehmann.stopandmove.movedistance;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.utils.LatLongDistanceFunction;

public class MoveDistance_Pisa {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PisaProblem problem = new PisaProblem(false);
		String moveTable = "stops_moves.pisa_move";
		List<SemanticTrajectory> trajs = problem.data();
		Map<Move, SemanticTrajectory> moves = new HashMap<>();
		for (SemanticTrajectory semanticTrajectory : trajs) {
			for (int j = 0; j < semanticTrajectory.length(); j++) {
				Move data = PisaDataReader.MOVE_ANGLE_SEMANTIC.getData(semanticTrajectory, j);
				if(data != null && !moves.containsKey(data)) {
					moves.put(data, semanticTrajectory);
				}
			}
		}
		new MoveDistance("pisa", new LatLongDistanceFunction()).extractMovementTraveledDistance(moveTable, moves);
	}
}
