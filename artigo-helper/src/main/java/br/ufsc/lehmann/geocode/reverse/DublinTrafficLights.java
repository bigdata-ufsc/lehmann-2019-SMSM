package br.ufsc.lehmann.geocode.reverse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.utils.Distance;
import cc.mallet.util.FileUtils;

public class DublinTrafficLights {
	
	private static List<String> stopIds = new ArrayList<>();

	public static void main(String[] args) throws NumberFormatException, ZipException, IOException, ParseException, URISyntaxException {
		File trafficLightFile = new File("C:/Users/André/git/ArtigoMSM_UFSC/artigo/src/main/resources/datasets/dublin.stop.traffic_light.csv");
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
		DublinBusDataReader dublinBusDataReader = new DublinBusDataReader(true);
		List<Stop> exportStops = dublinBusDataReader.exportStops();
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
	}
}
