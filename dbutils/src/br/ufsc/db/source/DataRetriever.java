package br.ufsc.db.source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.DataManager;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public abstract class DataRetriever extends DataManager {
	protected DataSource source;
	
	public abstract void connect(DataSource source) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException;
	
	public abstract List<Trajectory> fetchTrajectories(boolean convert) throws SQLException;
	public abstract Trajectory fetchTrajectory(int tid, boolean convert) throws SQLException;
	public abstract Trajectory fetchTrajectoryTaxicab(int tid) throws SQLException;
	public abstract Set<Integer> fetchTIDs() throws SQLException;
	public abstract ResultSet fetchData(String sql) throws SQLException;
	
	public abstract void setBufferInMeters();
	public abstract Connection getConnection();
	
	public abstract List<Stop> fetchStops() throws SQLException;
	public abstract List<Move> fetchMoves() throws SQLException;

	public Trajectory fetchTrajectoryBy(String attribute, int tid,
			boolean convert) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void prepareFetchTrajectoryStatement() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Trajectory fastFetchTrajectory(int tid) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	public abstract List<Trajectory> fastFetchTrajectories() throws SQLException;

	public abstract List<Trajectory> fetchTrajectories(String word) throws SQLException;

	public abstract Set<Integer> fetchTIDs(int number) throws SQLException;

	public abstract List<Trajectory> veryFastFetchTrajectories() throws SQLException;
	
	public abstract List<Trajectory> fastFetchTrajectories(List<Integer> T) throws SQLException;

	public abstract List<Integer> fetchListTIDs() throws SQLException ;

}
