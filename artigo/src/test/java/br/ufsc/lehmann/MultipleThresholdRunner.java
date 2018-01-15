package br.ufsc.lehmann;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.runner.JUnitCore;

import br.ufsc.lehmann.classifier.AllClassifierTests;

public class MultipleThresholdRunner {

	public static void main(String[] args) {
		JUnitCore junit = new JUnitCore();
		String folder = "C:\\Users\\André\\git\\ArtigoMSM_UFSC\\artigo-helper\\src\\main\\resources\\only-stops-dataset";
		
		for (int spatialThreshold = 0; spatialThreshold <= 500; spatialThreshold += 50) {
			for (int timeThreshold = 0; timeThreshold <= 120; timeThreshold += 30) {
				for (double proportionalTimeThreshold = 0.0; proportionalTimeThreshold < 1.1; proportionalTimeThreshold += .2) {
					FileOutputStream out = null;
					try {
						File file = new File(folder
								+ String.format("\\AllClassifierTests Thresholds-%dms %dmin %.1fTimeProportion.out",
										spatialThreshold, timeThreshold, proportionalTimeThreshold));
						if(file.exists()) {
							file.delete();
						}
						file.createNewFile();
						out = new FileOutputStream(file);
						System.setOut(new PrintStream(new BufferedOutputStream(out), true));
						Thresholds.SPATIAL_EUCLIDEAN.setValue(spatialThreshold);
						Thresholds.SPATIAL_LATLON.setValue(spatialThreshold);
						Thresholds.TEMPORAL.setValue(proportionalTimeThreshold);
						Thresholds.SLACK_TEMPORAL.setValue(timeThreshold);
						junit.run(AllClassifierTests.class);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.flush();
								out.close();
							} catch (IOException e) {
							}
						}
					}
				}
			}
		}
		
	}
	
}
