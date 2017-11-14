package br.ufsc.lehmann.postgis.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import cc.mallet.util.IoUtils;

public class StopToStopTrajectoryTransformer {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		if(true) {
			fromCSV();
			return;
		}
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicle_stop", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("SELECT infered_trip_id, gid, semantic_stop_id, semantic_move_id "+
			  "from bus.nyc_20140927_zoned "+
			  "order by infered_trip_id,time_received");
		String currentTid = null;
		Long currentStopId = null, currentMoveId = null;
		Set<Long> gidsToRemove = new LinkedHashSet<>();
		while(rs.next()) {
			if(currentTid == null || !currentTid.equals(rs.getString("infered_trip_id"))) {
				currentTid = rs.getString("infered_trip_id");
				if(currentMoveId != null) {
					gidsToRemove.add(rs.getLong("gid"));
					currentMoveId = null;
				}
				currentStopId = null;
			}
			Long stopId = rs.getLong("semantic_stop_id");
			boolean isStop = !rs.wasNull();
			Long moveId = rs.getLong("semantic_move_id");
			boolean isMove = !rs.wasNull();
			if(isStop) {
				if(currentMoveId != null) {
					currentMoveId = null;
				}
				currentStopId = stopId;
			}
			if(isMove) {
				if(currentStopId == null) {
					gidsToRemove.add(rs.getLong("gid"));
				} else {
					currentStopId = null;
					currentMoveId = moveId;
				}
			}
		}
		System.out.println("gid in (" + gidsToRemove + ")");
		conn.commit();
	}
	
	private static void fromCSV() throws IOException {
		String dataFile = "./datasets/sanfrancisco.smot.data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(StopToStopTrajectoryTransformer.class.getClassLoader().getResource(dataFile).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("taxi.sanfrancisco_taxicab_subset_cleaned.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "tid", "taxi_id", "lat", "lon", "timestamp", "ocupation", "airport", "mall", "road", "direction", "intersection_101_280", "bayshore_fwy", "stop", "semantic_stop_id", "semantic_move_id", "route").withDelimiter(';'));
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		String currentTid = null;
		String currentStopId = null, currentMoveId = null;
		Set<String> gidsToRemove = new LinkedHashSet<>();
		Set<String> lastMoves = new LinkedHashSet<>();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(currentTid == null || !currentTid.equals(data.get("tid"))) {
				currentTid = data.get("tid");
				if(currentMoveId != null) {
					gidsToRemove.addAll(lastMoves);
					currentMoveId = null;
				}
				currentStopId = null;
			}
			String stop = data.get("semantic_stop_id");
			String move = data.get("semantic_move_id");
			boolean isStop = !StringUtils.isEmpty(stop);
			boolean isMove = !StringUtils.isEmpty(move);
			if(isStop) {
				lastMoves.clear();
				if(currentMoveId != null) {
					currentMoveId = null;
				}
				currentStopId = stop;
			}
			if(isMove) {
				lastMoves.add(data.get("gid"));
				if(currentStopId == null) {
					gidsToRemove.add(data.get("gid"));
				} else {
					currentMoveId = move;
				}
			}
		}
		pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		System.out.println("gids to remove: " + gidsToRemove);
		
		File directory = Files.createTempDirectory("trajs").toFile();
		File nycTrajs = new File(directory, "taxi.sanfrancisco_taxicab_subset_cleaned.csv");
		CSVPrinter printer = new CSVPrinter(new FileWriter(nycTrajs),
				CSVFormat.EXCEL.withHeader("gid", "tid", "taxi_id", "lat", "lon", "timestamp", "ocupation", "airport", "mall", "road", "direction", "intersection_101_280", "bayshore_fwy", "stop", "semantic_stop_id", "semantic_move_id", "route").withDelimiter(';'));
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(!gidsToRemove.contains(data.get("gid"))) {
				printer.printRecord(data);
			}
		}
		printer.flush();
		printer.close();
		System.out.println("Output -> " + nycTrajs.getAbsolutePath());
		zipFile.close();
	}
}