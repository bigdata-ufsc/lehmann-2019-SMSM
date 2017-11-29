package br.ufsc.lehmann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import cc.mallet.util.IoUtils;

public class GeolifeTransportationMeansEnricher {

	private static DataSource source;
	private static Connection conn;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, FileNotFoundException, IOException {
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.geolife2_limited", null, "geom");
		conn = source.getRetriever().getConnection();

		System.out.printf("Reading CSV file\n");

		String filename = "./geolifetrans.csv.zip";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(GeolifeTransportationMeansEnricher.class.getClassLoader().getResource(filename).getFile(), "UTF-8"));
		//
		InputStreamReader rawPointsEntry = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("geolifetrans.csv")));
		CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(rawPointsEntry).toString(), 
				CSVFormat.EXCEL.withHeader("gid", "tid", "date", "time", "lat", "lon", "timestamp", "geom", "altitude", "path", "folder_id", "mode").withDelimiter(','));
		List<CSVRecord> csvRecords = pointsParser.getRecords();
		csvRecords = csvRecords.subList(1, csvRecords.size() - 1);
		Multimap<String, Integer> transpToGids = MultimapBuilder.hashKeys().arrayListValues().build();
		System.out.printf("Grouping transportation modes\n");
		for (CSVRecord rec : csvRecords) {
			String mode = rec.get("mode");
			if(!StringUtils.isEmpty(mode)) {
				transpToGids.put(mode, Integer.parseInt(rec.get("gid")));
			}
		}
		csvRecords.clear();
		pointsParser.close();
		zipFile.close();
		Map<String, Collection<Integer>> asMap = transpToGids.asMap();
		conn.setAutoCommit(false);
		for (Map.Entry<String, Collection<Integer>> entry : asMap.entrySet()) {
			System.out.printf("Writing '%s' transportation mode in %d gids\n", entry.getKey(), entry.getValue().size());
			List<Integer> gids = new ArrayList<>(5000);
			Collection<Integer> value = entry.getValue();
			for (Integer gid : value) {
				gids.add(gid);
				if(gids.size() == 5000) {
					updatetransportationMode(entry.getKey(), gids);
					gids.clear();
					System.out.println("Batch...");
				}
			}
			if(!gids.isEmpty()) {
				updatetransportationMode(entry.getKey(), gids);
			}
			conn.commit();
		}
		conn.commit();
		conn.close();
	}

	private static void updatetransportationMode(String mode, List<Integer> gids) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("update public.geolife2_limited set transportation_mean = ? where gid in (SELECT * FROM unnest(?))");
		ps.setString(1, mode);
		Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
		ps.setArray(2, array);
		ps.execute();
		
	}
}
