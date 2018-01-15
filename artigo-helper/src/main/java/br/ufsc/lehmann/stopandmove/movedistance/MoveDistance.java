package br.ufsc.lehmann.stopandmove.movedistance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.utils.Distance;

public class MoveDistance {
	
	private final String database;
	private SpatialDistanceFunction func;
	public MoveDistance(SpatialDistanceFunction func) {
		this("postgis", func);
	}
	public MoveDistance(String database, SpatialDistanceFunction func) {
		this.database = database;
		this.func = func;
	}

	public void extractMovementTraveledDistance(String moveTable, Map<Move, SemanticTrajectory> moves)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, database, DataSourceType.PGSQL, moveTable, null, "geom");
		Connection conn = source.getRetriever().getConnection();
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement("update "+moveTable+" set traveled_distance = ? where move_id = ?");
		int registers = 0;
		for (Map.Entry<Move, SemanticTrajectory> entry : moves.entrySet()) {
			registers++;
			Move move = entry.getKey();
			TPoint[] points = new TPoint[move.getLength()];
			for (int i = 0; i < move.getLength(); i++) {
				points[i] = Semantic.SPATIAL.getData(entry.getValue(), move.getBegin() + i);
			}
			double traveledDistance = Distance.getDistance(points, func);
			ps.setDouble(1, traveledDistance);
			ps.setInt(2, move.getMoveId());
			ps.execute();
			if(registers % 100 == 0) {
				conn.commit();
			}
		}
		conn.commit();
		conn.close();
	}

}
