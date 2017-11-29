package br.ufsc.lehmann.stopandmove;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.DateUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_Regions_Problem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveCSVReader;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class GenerateCSV_SanFrancisco {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		SanFranciscoCab_Regions_Problem p = new SanFranciscoCab_Regions_Problem(SanFranciscoCabDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}, new String[] {"mall", "intersection_101_280", "bayshore_fwy", "airport"}, true);
		List<SemanticTrajectory> data = p.balancedData();
		List<Integer> tids = data.stream().mapToInt(S -> (Integer) S.getTrajectoryId()).boxed().collect(Collectors.toList());
		List<CSVRecord> rawData = p.rawData();
		
		File directory = Files.createTempDirectory("trajs").toFile();
		
		File pisaTrajs = new File(directory, "taxi.sanfrancisco_taxicab_subset_cleaned.csv");
		CSVPrinter printer = new CSVPrinter(new FileWriter(pisaTrajs),
				CSVFormat.EXCEL.withHeader("gid", "tid", "taxi_id", "lat", "lon", "timestamp", "ocupation", "airport", "mall", "road", "direction", "intersection_101_280", "bayshore_fwy", "stop", "semantic_stop_id", "semantic_move_id", "route").withDelimiter(';'));
		for (CSVRecord record : rawData) {
			if(tids.contains(Integer.parseInt(record.get("tid")))) {
				printer.printRecord(record);
			}
		}
		printer.flush();
		printer.close();

		System.out.println(pisaTrajs.getAbsolutePath());
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
