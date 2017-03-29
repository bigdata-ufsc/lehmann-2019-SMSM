package br.ufsc.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DataManager {
	protected Connection connection;

	public ResultSet executeStatement(String sql){
		Statement statement = null;
		ResultSet resultSet;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return resultSet;
	}
	
	public void execute(String sql){
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public ResultSet executeQuery(String sql){
		Statement statement = null;
		try {
			statement = connection.createStatement();
			return statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ResultSet executeQuery(String sql,int fetchSize){
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setFetchSize(fetchSize);
			return statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// for older versions compatibility purposes
	protected void executeNoErrorMsg(String sql){
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	}
	
	
}
