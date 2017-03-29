package br.ufsc.db.source;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.DBConstants;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class PGRetriever extends DataRetriever {
	
	PreparedStatement fetchTrajectory;

	@Override
	public void connect(DataSource source) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		this.source=source;
		Driver newDriver = (Driver) Class.forName("org.postgresql.Driver")
				.newInstance();
		DriverManager.registerDriver(newDriver);
		connection = DriverManager.getConnection("jdbc:postgresql://"+source.getHost()+":"+source.getPort()+"/"+source.getDatabase(),
				source.getUser(), source.getPass());
	}

	@Override
	public List<Trajectory> fetchTrajectories(boolean convert) throws SQLException {

		String sql = "SELECT gid,tid,time,ST_X(geom) as lon,ST_Y(geom) as lat from " +
				source.getTrajectoryTable()+" order by tid,time;";
		ResultSet resultSet = executeStatement(sql);

		Map<Integer,Trajectory> index = new HashMap<Integer,Trajectory>();
		while(resultSet.next()){
			Integer tid = resultSet.getInt("tid");
			if (!index.containsKey(tid)){
				index.put(tid, new Trajectory(tid));
			}
			Double x = resultSet.getDouble("lon"); 
			Double y = resultSet.getDouble("lat"); 
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			
			TPoint p = new TPoint(gid,x,y,time);
			
			index.get(tid).addPoint(p);
		}
		resultSet.close();
		
		List<Trajectory> result = new ArrayList<Trajectory>(index.values());
		return result;
	}
	
	@Override
	public List<Trajectory> fetchTrajectories(String word) throws SQLException {

		String sql = "SELECT gid,tid,time,ST_X(geom) as lon,ST_Y(geom) as lat from " +
				source.getTrajectoryTable()+" where word='"+word+"' order by tid,time;";
		ResultSet resultSet = executeStatement(sql);

		Map<Integer,Trajectory> index = new HashMap<Integer,Trajectory>();
		while(resultSet.next()){
			Integer tid = resultSet.getInt("tid");
			if (!index.containsKey(tid)){
				index.put(tid, new Trajectory(tid));
			}
			Double x = resultSet.getDouble("lon"); 
			Double y = resultSet.getDouble("lat"); 
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			
			TPoint p = new TPoint(gid,x,y,time);
			
			index.get(tid).addPoint(p);
		}
		resultSet.close();
		
		List<Trajectory> result = new ArrayList<Trajectory>(index.values());
		return result;
	}
	
	
	

	@Override
	public List<Trajectory> fastFetchTrajectories() throws SQLException {

		String sql = "SELECT gid,tid,time,ST_X(geom) as lon,ST_Y(geom) as lat from " +
				source.getTrajectoryTable()+" order by tid,time;";
		ResultSet resultSet = executeStatement(sql);
		System.out.println("Query result...");
		Map<Integer,Trajectory> index = new HashMap<Integer,Trajectory>();
		while(resultSet.next()){
			int tid = resultSet.getInt("tid");
			if (!index.containsKey(tid)){
				index.put(tid, new Trajectory(tid));
			}
			double x = resultSet.getDouble("lon"); 
			double y = resultSet.getDouble("lat"); 
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			
			TPoint p = new TPoint(gid,x,y,time);
			
			index.get(tid).addPoint(p);
		}
		resultSet.close();
		System.out.println("Creating ArrayList...");
		List<Trajectory> result = new ArrayList<Trajectory>(index.values());
		return result;
	}
	
	public List<Trajectory> fastFetchTrajectories(List<Integer> T) throws SQLException {
		
		String TIDs = concatSet(T, ",");
		
		String sql = "SELECT gid,tid,st_x(geom) as lon, st_y(geom) as lat,time from " +
				source.getTrajectoryTable()+" WHERE tid IN("+TIDs+") order by tid,time;";
		ResultSet resultSet = executeStatement(sql);
		System.out.println("Query result...");
		Map<Integer,Trajectory> index = new HashMap<Integer,Trajectory>();
		while(resultSet.next()){
			int tid = resultSet.getInt("tid");
			if (!index.containsKey(tid)){
				index.put(tid, new Trajectory(tid));
			}
			double x = resultSet.getDouble("lon"); 
			double y = resultSet.getDouble("lat"); 
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			
			TPoint p = new TPoint(gid,x,y,time);
			
			index.get(tid).addPoint(p);
		}
		resultSet.close();
		System.out.println("Creating ArrayList...");
		List<Trajectory> result = new ArrayList<Trajectory>(index.values());
		return result;
	}
	
	public static String concatSet(List<Integer> S, String separator) {
	    StringBuilder sb = new StringBuilder();
	    String sep = "";
	    for(Integer s: S) {
	        sb.append(sep).append(s);
	        sep = separator;
	    }
	    return sb.toString();                           
	}
	
	
	//@Override
	public List<Trajectory> veryFastFetchTrajectories() throws SQLException {

		String sql = "SELECT gid,tid,time,ST_X(geom) as lon,ST_Y(geom) as lat from " +
				source.getTrajectoryTable()+" order by tid,time;";
		
		ResultSet resultSet = executeQuery(sql,1000000);
		System.out.println("Query result...");
		Map<Integer,Trajectory> index = new HashMap<Integer,Trajectory>();
		while(resultSet.next()){
			int tid = resultSet.getInt("tid");
			if (!index.containsKey(tid)){
				index.put(tid, new Trajectory(tid));
			}
			double x = resultSet.getDouble("lon"); 
			double y = resultSet.getDouble("lat"); 
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			
			TPoint p = new TPoint(gid,x,y,time);
			
			index.get(tid).addPoint(p);
		}
		resultSet.close();
		System.out.println("Creating ArrayList...");
		List<Trajectory> result = new ArrayList<Trajectory>(index.values());
		return result;
	}
	

	@Override
	public Trajectory fetchTrajectory(int tid, boolean convert) throws SQLException {
		String addition = "";
		if(convert)
			addition = ",ST_X(ST_TRANSFORM("+DBConstants.TRAJECTORY_GEOM+","+DBConstants.SRID+")) as " + DBConstants.TRAJECTORY_X_TRANSF_ALIAS+
					",ST_Y(ST_TRANSFORM("+DBConstants.TRAJECTORY_GEOM+","+DBConstants.SRID+")) as " + DBConstants.TRAJECTORY_Y_TRANSF_ALIAS;
		
		String sql = "SELECT gid,tid,time,ST_X(geom) as "+
					DBConstants.TRAJECTORY_X_ALIAS+",ST_Y(geom) as "+DBConstants.TRAJECTORY_Y_ALIAS+ addition +
					" from "+ source.getTrajectoryTable()+
					" where tid="+tid+" order by tid,time;";
		ResultSet resultSet = executeStatement(sql);
		Trajectory result = new Trajectory(tid);
		while(resultSet.next()){
			Double x = resultSet.getDouble(DBConstants.TRAJECTORY_X_ALIAS);
			Double y = resultSet.getDouble(DBConstants.TRAJECTORY_Y_ALIAS);
			Timestamp time = resultSet.getTimestamp(DBConstants.TRAJECTORY_TIME);
			int gid = resultSet.getInt("gid");
			TPoint p;
			if(convert) {
				//System.out.println("1");
				p = new TPoint(gid,x,y,time,resultSet.getDouble(DBConstants.TRAJECTORY_X_TRANSF_ALIAS),resultSet.getDouble(DBConstants.TRAJECTORY_Y_TRANSF_ALIAS));
			}
			else {
				//System.out.println("2");
				p = new TPoint(gid,x,y,time,x,y);
			}
			result.addPoint(p);
		}
		resultSet.close();
		
		return result;
	}
	
	@Override
	public void prepareFetchTrajectoryStatement() throws SQLException{
		String sql =
		        "SELECT gid,tid,time,st_x(geom) as lon,st_y(geom) as lat from " + source.getTrajectoryTable() +
		        " where tid = ? order by tid,time;";
		fetchTrajectory = this.connection.prepareStatement(sql);
	}
	
	@Override
	public Trajectory fastFetchTrajectory(int tid) throws SQLException {
		
		fetchTrajectory.setInt(1,tid);
		ResultSet resultSet = fetchTrajectory.executeQuery();
		Trajectory result = new Trajectory(tid);
		while(resultSet.next()){			
			TPoint p= new TPoint(resultSet.getInt("gid"),resultSet.getDouble("lon"),resultSet.getDouble("lat"),resultSet.getTimestamp("time"));
			result.addPoint(p);
		}
		resultSet.close();
		
		return result;
	}
	
	@Override
	public Trajectory fetchTrajectoryTaxicab(int tid) throws SQLException {
		String sql = "SELECT gid,tid,time,st_x(geom) as lon,st_y(geom) as lat,occupation" +
					" from "+ source.getTrajectoryTable()+
					" where tid="+tid+" order by tid,time;";
		ResultSet resultSet = executeStatement(sql);
		Trajectory result = new Trajectory(tid);
		while(resultSet.next()){
			Double x = resultSet.getDouble("lon");
			Double y = resultSet.getDouble("lat");
			Timestamp time = resultSet.getTimestamp("time");
			int gid = resultSet.getInt("gid");
			int occupation = resultSet.getInt("occupation");
			TPoint p= new TPoint(gid,x,y,time,occupation);
			result.addPoint(p);
		}
		resultSet.close();
		
		return result;
	}
	
	@Override
	public Trajectory fetchTrajectoryBy(String attribute,int value, boolean convert) throws SQLException {
		String addition = "";
		if(convert)
			addition = ",ST_X(ST_TRANSFORM("+DBConstants.TRAJECTORY_GEOM+","+DBConstants.SRID+")) as " + DBConstants.TRAJECTORY_X_TRANSF_ALIAS+
					",ST_Y(ST_TRANSFORM("+DBConstants.TRAJECTORY_GEOM+","+DBConstants.SRID+")) as " + DBConstants.TRAJECTORY_Y_TRANSF_ALIAS;
		
		String sql = "SELECT gid,"+DBConstants.TRAJECTORY_ID+","+DBConstants.TRAJECTORY_TIME+",ST_X("+DBConstants.TRAJECTORY_GEOM+") as "+
					DBConstants.TRAJECTORY_X_ALIAS+",ST_Y("+DBConstants.TRAJECTORY_GEOM+") as "+DBConstants.TRAJECTORY_Y_ALIAS+ addition +
					" from "+ source.getTrajectoryTable()+
					" where "+attribute+"="+value+" order by "+DBConstants.TRAJECTORY_ID+","+DBConstants.TRAJECTORY_TIME+";";
		ResultSet resultSet = executeStatement(sql);
		Trajectory result = new Trajectory(resultSet.getInt("tid"));
		while(resultSet.next()){
			Double x = resultSet.getDouble(DBConstants.TRAJECTORY_X_ALIAS);
			Double y = resultSet.getDouble(DBConstants.TRAJECTORY_Y_ALIAS);
			Timestamp time = resultSet.getTimestamp(DBConstants.TRAJECTORY_TIME);
			int gid = resultSet.getInt("gid");
			TPoint p;
			if(convert) {
				//System.out.println("1");
				p = new TPoint(gid,x,y,time,resultSet.getDouble(DBConstants.TRAJECTORY_X_TRANSF_ALIAS),resultSet.getDouble(DBConstants.TRAJECTORY_Y_TRANSF_ALIAS));
			}
			else {
				//System.out.println("2");
				p = new TPoint(gid,x,y,time,x,y);
			}
			result.addPoint(p);
		}
		resultSet.close();
		
		return result;
	}

	@Override
	public Set<Integer> fetchTIDs() throws SQLException {
		String sql = "SELECT DISTINCT "+DBConstants.TRAJECTORY_ID+" from "+source.getTrajectoryTable()+";";
		ResultSet resultSet = executeStatement(sql);

		Set<Integer> result = new HashSet<Integer>();
		while(resultSet.next()){
			//Integer tid = resultSet.getInt(DBConstants.TRAJECTORY_ID);
			result.add(resultSet.getInt(DBConstants.TRAJECTORY_ID));
		}
		resultSet.close();

		return result;
	}
	
	@Override
	public List<Integer> fetchListTIDs() throws SQLException {
		String sql = "select tid FROM (SELECT DISTINCT "+DBConstants.TRAJECTORY_ID+" from "+source.getTrajectoryTable()+") as b order by random();";
		ResultSet resultSet = executeStatement(sql);

		List<Integer> result = new ArrayList<Integer>();
		while(resultSet.next()){
			//Integer tid = resultSet.getInt(DBConstants.TRAJECTORY_ID);
			result.add(resultSet.getInt(DBConstants.TRAJECTORY_ID));
		}
		resultSet.close();

		return result;
	}
	
	@Override
	public Set<Integer> fetchTIDs(int number) throws SQLException {
		//String sql = "SELECT DISTINCT tid from "+source.getTrajectoryTable()+" ORDER BY random() LIMIT 1000;";
		
		
		String sql = "SELECT tid FROM (select distinct tid from "+source.getTrajectoryTable()+") as a ORDER BY RANDOM()	LIMIT "+number+";";
		ResultSet resultSet = executeStatement(sql);

		Set<Integer> result = new HashSet<Integer>();
		while(resultSet.next()){
			Integer tid = resultSet.getInt("tid");
			result.add(tid);
		}
		resultSet.close();

		return result;
	}

	@Override
	public void setBufferInMeters() {
		String sql = "CREATE OR REPLACE FUNCTION utmzone(geometry) " +
		" RETURNS integer AS " +
		" $BODY$ " +
		" DECLARE " +
		" geomgeog geometry; " +
		" zone int; " +
		" pref int; " +
		 
		" BEGIN " +
		" geomgeog:= ST_Transform($1,4326); " +
		 
		" IF (ST_Y(geomgeog))>0 THEN " +
		" pref:=32600; " +
		" ELSE " +
		" pref:=32700; " +
		" END IF; " +
		 
		" zone:=floor((ST_X(geomgeog)+180)/6)+1; " +
		 
		" RETURN zone+pref; " +
		" END; " +
		" $BODY$ LANGUAGE 'plpgsql' IMMUTABLE " +
		" COST 100; ";
		
		execute(sql);
		
		sql = "CREATE OR REPLACE FUNCTION ST_Buffer_Meters(geometry, double precision) " +
		" RETURNS geometry AS " +
		" $BODY$ " +
		" DECLARE " +
		" orig_srid int; " +
		" utm_srid int; " +
		 
		" BEGIN " +
		" orig_srid:= ST_SRID($1); " +
		" utm_srid:= utmzone(ST_Centroid($1)); " +
		 
		" RETURN ST_transform(ST_Buffer(ST_transform($1, utm_srid), $2), orig_srid); " +
		" END; " +
		" $BODY$ LANGUAGE 'plpgsql' IMMUTABLE " +
		" COST 100;";
		
		execute(sql);
	}
	
//	@Override
//	public List<CandidateStop> fetchCandidates() throws SQLException {
//		List<CandidateStop> result = new ArrayList<CandidateStop>();
//
//		Set<String> tables = source.getFeatureTables();
//		for(String table : tables){
//			String sql = "SELECT "+DBConstants.CANDIDATE_ID+","+DBConstants.CANDIDATE_NAME+","+
//					DBConstants.CANDIDATE_GEOM+","+DBConstants.CANDIDATE_TIME+" from "+ source.getFeatureTables()+";";
//			ResultSet resultSet = executeStatement(sql);
//
//			while(resultSet.next()){
//				Integer id = resultSet.getInt(DBConstants.CANDIDATE_ID);
//				String name = resultSet.getString(DBConstants.CANDIDATE_NAME);
//				String geom = resultSet.getString(DBConstants.CANDIDATE_GEOM);
//				Long time = resultSet.getLong(DBConstants.CANDIDATE_TIME);
//				
//				result.add(new CandidateStop(id, table, name, geom, time));
//			}
//			
//			resultSet.close();
//		}
//		return result;
//	}

	@Override
	public ResultSet fetchData(String sql) throws SQLException {
		ResultSet resultSet = executeStatement(sql);
		return resultSet;
	}
	
//	@Override
//	public Set<CandidateStop> fecthIntersectedFeatures(Integer trajectoryID, Hashtable<String, CandidateStopCols> candidateCols, double buffer) throws SQLException{		
//		//TODO arrumar esse método no futuro para usar constantes em tudo e para retornar todos os dados do candidate stop
//		Set<CandidateStop> candidateStops = new HashSet<CandidateStop>();
//		
//		ResultSet resultSet, rs;
//		int srid = -1;
//		String geomCol = source.getGeomCol();
//		
//		String candidateName;
//		long candidateTime;
//		String candidateGeom;
//		
//		for(String table : source.getFeatureTables()){
//			rs = executeStatement("select st_srid("+geomCol+") as srid from "+table+" limit 1");
//			rs.next();
//			srid = rs.getInt("srid");
//			rs.close();
//			
//			candidateName = candidateCols.get(table).getName();
//			candidateTime = candidateCols.get(table).getTime();
//			candidateGeom = candidateCols.get(table).getGeom();
//			
//			if(candidateGeom.toLowerCase().contains("line") || candidateGeom.toLowerCase().contains("point")){
//				String sql = "CREATE TABLE " + table+"_buf (" + "gid serial NOT NULL, "+candidateName+" character varying,"+
//						"    CONSTRAINT "+table+"_buf_gidkey PRIMARY KEY (gid)"+") WITHOUT OIDS;";
//				execute(sql);
//				execute("SELECT AddGeometryColumn('"+table+"_buf', '"+geomCol+"',"+srid+", 'MULTIPOLYGON', 2);");
//				
//				execute("create index " + table + "_bufidx on " + table + "_buf using gist ("+geomCol+");");
//				
//				execute("insert into " + table + "_buf (gid, "+candidateName+", "+geomCol+") (select gid, "+candidateName+", ST_Multi(ST_Buffer_Meters("+geomCol+","+buffer+")) from "+table+");");
//				
//				sql = "select f."+candidateName+",f.gid,ST_AsText(f."+geomCol+") as wkt from " + table
//						+ "_buf as f," + source.getTrajectoryTable() +
//						" as t where tid=" + trajectoryID +
//						" and st_intersects(t."+geomCol+",f."+geomCol+");";
//				
//				resultSet = executeStatement(sql);
//				
//				execute("drop table " + table + "_buf cascade;");
//				
//				executeNoErrorMsg("delete from geometry_columns where f_table_name = " + table + "_buf;");
//			} else {
//				//System.out.println("******** tableName "+table);
//				String sql = "select f."+candidateName+",f.gid,ST_AsText(f."+geomCol+") as wkt from " + table
//						+ " as f," + source.getTrajectoryTable() +
//						" as t where tid=" + trajectoryID +
//						" and st_intersects(t."+geomCol+",f."+geomCol+");";
//				
//				resultSet = executeStatement(sql);
//			}
//			
//			while(resultSet.next()){
//				Integer gid = resultSet.getInt("gid");
//				String name = resultSet.getString(candidateName);
//				String geom = resultSet.getString("wkt");
//
//				CandidateStop candidateStop = new CandidateStop(gid,table,name,geom,candidateTime);
//				candidateStops.add(candidateStop);
//			}
//			
//			resultSet.close();
//			
//		}
//		
//		return candidateStops;
//	}
	
	public Connection getConnection(){
		return connection;
	}

	
	@Override
	public List<Stop> fetchStops() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Move> fetchMoves() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
