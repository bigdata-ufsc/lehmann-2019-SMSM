package br.ufsc.lehmann;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import cc.mallet.util.IoUtils;

public class GeolifeTransportationModeLabelFilesReader {
	
	static Pattern p = Pattern.compile(".+(\\d{3}).+");

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		String file = "./datasets/Geolife_TransportationMode_LabelFiles.zip";
		String outputFile = "./src/main/resources/datasets/Geolife_TransportationMode_Label.csv";
		ZipFile zipFile = new ZipFile(java.net.URLDecoder.decode(GeolifeTransportationModeLabelFilesReader.class.getClassLoader().getResource(file).getFile(), "UTF-8"));
		FileWriter out = new FileWriter(outputFile);
		CSVPrinter output = new CSVPrinter(out, CSVFormat.EXCEL.withHeader("Folder_id", "Start Time", "End Time", "Transportation Mode").withDelimiter(';'));
		zipFile.stream().filter(entry -> !entry.isDirectory()).forEach(entry -> {
			try {
				String folderId = entry.getName();
				Matcher matcher = p.matcher(folderId);
				if(matcher.matches()) {
					folderId = matcher.group(1);
					int userId = Integer.parseInt(folderId, 10) + 1;
					InputStreamReader labelFileEntry = new InputStreamReader(zipFile.getInputStream(entry));
					CSVParser pointsParser = CSVParser.parse(IoUtils.contentsAsCharSequence(labelFileEntry).toString(), 
							CSVFormat.EXCEL.withHeader("Start Time", "End Time", "Transportation Mode").withDelimiter('\t'));
					Iterator<CSVRecord> pointsData = pointsParser.iterator();
					while(pointsData.hasNext()) {
						CSVRecord data = pointsData.next();
						if(data.getRecordNumber() == 1) {
							//skip header
							continue;
						}
						output.printRecord(userId, data.get("Start Time"), data.get("End Time"), data.get("Transportation Mode"));
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		});
		zipFile.close();
		output.flush();
		output.close();
	}
}
