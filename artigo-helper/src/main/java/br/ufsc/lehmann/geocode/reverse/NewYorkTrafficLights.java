package br.ufsc.lehmann.geocode.reverse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDatabaseReader;
import br.ufsc.utils.Distance;
import cc.mallet.util.FileUtils;

public class NewYorkTrafficLights {
	
	private static List<String> stopIds = new ArrayList<>();

	public static void main(String[] args) throws NumberFormatException, ZipException, IOException, ParseException, URISyntaxException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		File trafficLightFile = new File("C:/Users/uuario/Desktop/Lehmann/ArtigoMSM_UFSC/artigo/src/main/resources/datasets/nyc.stop.BM2-BM3.traffic_light.csv");
		if(trafficLightFile.exists()) {
			String[] lines = FileUtils.readFile(trafficLightFile);
			for (int i = 0; i < lines.length; i++) {
				stopIds.add(lines[i].split(";")[0]);
			}
		} else {
			trafficLightFile.createNewFile();
		}
		FileWriter csvWriter = new FileWriter(trafficLightFile, true);
		TrafficLightsExtractor extractor = new TrafficLightsExtractor();
		NewYorkBusDatabaseReader dublinBusDataReader = new NewYorkBusDatabaseReader(true);
		List<Stop> exportStops = dublinBusDataReader.exportStops("MTABC_BM2", "MTABC_BM3");
		if(stopIds.isEmpty()) {
			csvWriter.write("stopId;trafficLightId;trafficLightDistance\n");
			csvWriter.flush();
		}
		int i = 0, count = exportStops.size();
		for (Stop stop : exportStops) {
			if(stopIds.contains(String.valueOf(stop.getStopId()))) {
				continue;
			}
			List<TrafficLight> lights = extractor.retrieveFrom(stop);
			TrafficLight nearest = null;
			double maxDistance = Double.MAX_VALUE;
			TPoint centroid = stop.getCentroid();
			for (TrafficLight light : lights) {
				double distance = Distance.distFrom(TrafficLightsExtractor.pointFromLatLon(light.getPosition()), centroid);
				if(nearest == null || distance < maxDistance) {
					nearest = light;
					maxDistance = distance;
				}
			}
			if(nearest != null) {
				stop.setTrafficLight(nearest.getId());
				stop.setTrafficLightDistance(maxDistance);
				csvWriter.write(String.format(Locale.US, "%d;%d;%.2f\n", stop.getStopId(), nearest.getId(), maxDistance));
				System.out.printf("%.1f\n", (i / (double) count) * 100.0);
				csvWriter.flush();
			}
		}
		csvWriter.close();
	}
}
