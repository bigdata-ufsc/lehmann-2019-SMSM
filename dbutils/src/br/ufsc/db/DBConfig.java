package br.ufsc.db;

import java.util.logging.Logger;

public class DBConfig {

	static final String driverPostgres = "org.postgresql.Driver";
	static final String url = "jdbc:postgresql://localhost/";
	static final String banco = "Amsterdam";
	static final String usuario = "postgres";
	static final String senha = "postgres";
	

	final static Logger LOGGER = Logger.getLogger(DBConnectionProvider.class.getName());

}
