package br.ufsc.lehmann.stopandmove;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class GenerateZippedFile_NewYorkBus_Zoned {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.bus_nyc_20140927_zoned_stop", null, "geom");

		Connection conn = source.getRetriever().getConnection();

		String sql = "select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
				+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
				+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id,\"POI\", semantic_stop_id, semantic_move_id "
				+ "from bus.nyc_20140927_zoned ";
		sql += "order by trip_id,time_received,gid";
		File directory = Files.createTempDirectory("trajs").toFile();
		
		File pisaTrajs = new File(directory, "bus.nyc_20140927.csv");
		CSVPrinter printer = new CSVPrinter(new FileWriter(pisaTrajs),
				CSVFormat.EXCEL.withHeader("gid", "time", "vehicle_id", "route", "trip_id", "longitude", "latitude", "distance_along_trip", "infered_direction_id", "phase", "next_scheduled_stop_distance", "next_scheduled_stop_id", "POI", "semantic_stop_id", "semantic_move_id").withDelimiter(';'));
		ResultSet data = conn.createStatement().executeQuery(sql);
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
			}
			Integer move = data.getInt("semantic_move_id");
			if(data.wasNull()) {
				move = null;
			}
			printer.printRecord(
					data.getInt("gid"),
					data.getTimestamp("time"),
					data.getInt("vehicle_id"),
					data.getString("route"),
					data.getString("trip_id"),
					data.getDouble("longitude"),
					data.getDouble("latitude"),
					data.getDouble("distance_along_trip"),
					data.getInt("infered_direction_id"),
					data.getString("phase"),
					data.getDouble("next_scheduled_stop_distance"),
					data.getString("next_scheduled_stop_id"),
					data.getString("POI"),
					stop,
					move);
		}
		printer.flush();
		printer.close();
		
		sql = "SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
				"centroid_lon, start_time, end_time, street, \"POI\" " + //
				"FROM stops_moves.bus_nyc_20140927_zoned_stop";

		File pisaStops = new File(directory, "stops_moves.bus_nyc_20140927_stop.csv");
		printer = new CSVPrinter(new FileWriter(pisaStops),
				CSVFormat.EXCEL.withHeader("stop_id", "start_lat", "start_lon", "end_lat", "end_lon", "centroid_lat", "centroid_lon", "start_time", "end_time", "begin", "length", "street", "POI").withDelimiter(';'));
		ResultSet stopsData = conn.createStatement().executeQuery(sql);
		while(stopsData.next()) {
			printer.printRecord(
					stopsData.getInt("stop_id"), //
					stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon"), //
					stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon"), //
					stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon"),//
					simpleDateFormat.format(stopsData.getTimestamp("start_time")), //
					simpleDateFormat.format(stopsData.getTimestamp("end_time")), //
					stopsData.getInt("begin"), //
					stopsData.getInt("length"), //
					stopsData.getString("street"),
					stopsData.getString("POI"));
		}
		printer.flush();
		printer.close();

		sql = "SELECT move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length " + //
				"FROM stops_moves.bus_nyc_20140927_zoned_move";

		File pisaMoves = new File(directory, "stops_moves.bus_nyc_20140927_move.csv");
		printer = new CSVPrinter(new FileWriter(pisaMoves),
				CSVFormat.EXCEL.withHeader("move_id", "start_time", "start_stop_id", "begin", "end_time", "end_stop_id", "length", "end_lon").withDelimiter(';'));
		ResultSet movesData = conn.createStatement().executeQuery(sql);
		while(movesData.next()) {
			int startStopId = movesData.getInt("start_stop_id");
			if (movesData.wasNull()) {
				startStopId = -1;
			}
			int endStopId = movesData.getInt("end_stop_id");
			if (movesData.wasNull()) {
				endStopId = -1;
			}
			printer.printRecord(
					movesData.getInt("move_id"), //
					simpleDateFormat.format(movesData.getTimestamp("start_time")), //
					startStopId, //
					movesData.getInt("begin"), //
					simpleDateFormat.format(movesData.getTimestamp("end_time")), //
					endStopId, //
					movesData.getInt("length"), //
					null);
		}
		printer.flush();
		printer.close();
		compact("../artigo/src/main/resources/datasets/nyc.smot.data.zip", pisaTrajs, pisaStops, pisaMoves);
	}

	public static void compact(String zipName, File... files) {

		try {
			FileOutputStream fos = new FileOutputStream(zipName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (int i = 0; i < files.length; i++) {
				addToZipFile(files[i], zos);
			}

			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void addToZipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {

		System.out.println("Writing '" + file.getName() + "' to zip file");

		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(file.getName());
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}

}
