package br.ufsc.db.destination;

import java.sql.SQLException;
import java.util.List;

import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.DataManager;

public abstract class DataStorer extends DataManager {
	protected DataStorage storage;
	
	public abstract void connect(DataStorage source) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException;
	
	public abstract void storeStops(List<Stop> stops, int srid) throws SQLException;
	public abstract void storeMoves(List<Move> moves) throws SQLException;
}
