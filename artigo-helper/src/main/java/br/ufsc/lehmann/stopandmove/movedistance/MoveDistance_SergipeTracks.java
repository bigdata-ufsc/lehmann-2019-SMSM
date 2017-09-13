package br.ufsc.lehmann.stopandmove.movedistance;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.stopandmove.LatLongDistanceFunction;

public class MoveDistance_SergipeTracks {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		SergipeTracksProblem problem = new SergipeTracksProblem();
		String moveTable = "stops_moves.sergipe_tracks_move";
		List<SemanticTrajectory> trajs = problem.data();
		Map<Move, SemanticTrajectory> moves = new HashMap<>();
		for (SemanticTrajectory semanticTrajectory : trajs) {
			for (int j = 0; j < semanticTrajectory.length(); j++) {
				Move data = SergipeTracksDataReader.MOVE_ANGLE_SEMANTIC.getData(semanticTrajectory, j);
				if(data != null && !moves.containsKey(data)) {
					moves.put(data, semanticTrajectory);
				}
			}
		}
		new MoveDistance(new LatLongDistanceFunction()).extractMovementTraveledDistance(moveTable, moves);
	}
}
