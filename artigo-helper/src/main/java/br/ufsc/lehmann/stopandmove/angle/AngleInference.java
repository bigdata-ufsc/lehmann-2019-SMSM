package br.ufsc.lehmann.stopandmove.angle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.utils.Angle;

public class AngleInference {
	private final String database;

	public AngleInference() {
		this("postgis");
	}

	public AngleInference(String database) {
		this.database = database;
	}

	public void extract(String moveTable, Map<Move, SemanticTrajectory> moves)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, database, DataSourceType.PGSQL, moveTable, null, "geom");
		Connection conn = source.getRetriever().getConnection();
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement("update " + moveTable + " set angle = ? where move_id = ?");
		int registers = 0;
		for (Map.Entry<Move, SemanticTrajectory> entry : moves.entrySet()) {
			registers++;
			Move move = entry.getKey();
			TPoint start = Semantic.GEOGRAPHIC.getData(entry.getValue(), move.getBegin());
			TPoint end = null;
			int endIndex = move.getBegin() + move.getLength() - 1;
			end = Semantic.GEOGRAPHIC.getData(entry.getValue(), endIndex);
			double angle = Angle.getAngle(start, end);
			ps.setDouble(1, angle);
			ps.setInt(2, move.getMoveId());
			ps.execute();
			if (registers % 100 == 0) {
				conn.commit();
			}
		}
		conn.commit();
		conn.close();
	}

	public static void extractMovementAngle(String moveTable, Map<Move, SemanticTrajectory> moves)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		new AngleInference().extract(moveTable, moves);
	}
}
