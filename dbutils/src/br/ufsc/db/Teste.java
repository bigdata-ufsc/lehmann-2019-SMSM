package br.ufsc.db;

import java.sql.SQLException;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Teste {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgis", "localhost", 5432, "mobilidade", DataSourceType.PGSQL, "mob_trajrj", null,"geom");
		DataRetriever retriever = source.getRetriever();
		System.out.println(retriever.fetchTIDs().size());
		Trajectory traj = retriever.fetchTrajectory(1, false);
		for (TPoint p : traj.getPoints()) {
			System.out.println(p.getTime());
		}
	}

}
