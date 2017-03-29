package br.ufsc.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import br.ufsc.db.source.DataRetriever;

public class DBUtils {
	public static int getSequenceNextValue(final String sequenceName, final Statement st)
			throws SQLException {
		ResultSet sequenceTid = st.executeQuery("SELECT nextVal('"
				+ sequenceName + "');");
		sequenceTid.next();
		return sequenceTid.getInt("nextVal");
	}
	
	public static int getSequenceNextValue(final String sequenceName, final DataRetriever retriever)
			throws SQLException {
		ResultSet sequenceTid = retriever.executeQuery("SELECT nextVal('"
				+ sequenceName + "');");
		sequenceTid.next();
		return sequenceTid.getInt("nextVal");
	}
}
