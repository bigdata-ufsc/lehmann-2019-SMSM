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
import java.util.stream.Collectors;
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
//		if(true) {
//			fromCSV();
//			return;
//		}
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.geolife_enriched_move", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		while(true) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT tid, gid, semantic_stop_id, semantic_move_id "+
					"from geolife.geolife_enriched_transportation_means "+
					"order by tid,time,gid");
			Integer currentTid = null;
			Set<Long> gidsToRemove = new LinkedHashSet<>();
			Long currentStopId = null, currentMoveId = null;
			Set<Long> lastGids = new LinkedHashSet<>();
			while(rs.next()) {
				if(currentTid == null || !currentTid.equals(rs.getInt("tid"))) {
					currentTid = rs.getInt("tid");
					if(!lastGids.isEmpty()) {
						gidsToRemove.addAll(lastGids);
						lastGids.clear();
					}
					currentMoveId = null;
					currentStopId = null;
				}
				Long stopId = rs.getLong("semantic_stop_id");
				boolean isStop = !rs.wasNull();
				Long moveId = rs.getLong("semantic_move_id");
				boolean isMove = !rs.wasNull();
				if(isStop) {
					if(currentStopId == null) {
						if(!lastGids.isEmpty()) {
							gidsToRemove.addAll(lastGids);
						}
					}
					currentStopId = stopId;
					lastGids.clear();
				}
				if(isMove) {
					long lastGid = rs.getLong("gid");
					currentMoveId = moveId;
					lastGids.add(lastGid);
				}
			}
			if(!lastGids.isEmpty()) {
				gidsToRemove.addAll(lastGids);
			}
			System.out.println(gidsToRemove.size());
			if(gidsToRemove.isEmpty()) {
				return;
			}
			int i = 0;
			for (; i < gidsToRemove.size(); i+=100000) {
				int min = Math.min(gidsToRemove.size() - i, 100000);
				System.out.printf("Deleting %d rows\n", min);
				List<Long> collect = gidsToRemove.stream().skip(i).limit(min).collect(Collectors.toList());
				String string = collect.toString();
				
				conn.prepareStatement("delete from geolife.geolife_enriched_transportation_means where gid in (" + string.substring(1, string.length() - 1) + ")").execute();
				conn.commit();
			}
		}
	}
	
	private static void fromCSV() throws IOException {
		String dataFile = "./datasets/nyc.smot.data.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(StopToStopTrajectoryTransformer.class.getClassLoader().getResource(dataFile).getFile(), "UTF-8"));
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("bus.nyc_20140927.csv")));
		CSVFormat csvHeader = CSVFormat.EXCEL.withHeader("gid", "time", "vehicle_id", "route", "trip_id", "longitude", "latitude", "distance_along_trip", "infered_direction_id", "phase", "next_scheduled_stop_distance", "next_scheduled_stop_id", "POI", "semantic_stop_id", "semantic_move_id").withDelimiter(';');
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), csvHeader);
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		Iterator<CSVRecord> pointsData = csvRecords.subList(1, csvRecords.size()).iterator();
		String currentTid = null;
		String currentStopId = null, currentMoveId = null;
		Set<String> gidsToRemove = new LinkedHashSet<>();
		Set<String> lastMoves = new LinkedHashSet<>();
		while(pointsData.hasNext()) {
			CSVRecord data = pointsData.next();
			if(currentTid == null || !currentTid.equals(data.get("trip_id"))) {
				currentTid = data.get("trip_id");
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
		File nycTrajs = new File(directory, "bus.nyc_20140927.csv");
		CSVPrinter printer = new CSVPrinter(new FileWriter(nycTrajs), csvHeader);
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
