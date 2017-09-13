package br.ufsc.lehmann.stopandmove.angle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;

public class AngleInference_Pisa {

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
		new AngleInference("pisa").extract(moveTable, moves);
	}
}
