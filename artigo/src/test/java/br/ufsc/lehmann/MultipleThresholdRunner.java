package br.ufsc.lehmann;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.runner.JUnitCore;

import br.ufsc.lehmann.classifier.DTWaClassifierTest;
import br.ufsc.lehmann.classifier.EDRClassifierTest;
import br.ufsc.lehmann.classifier.LCSSClassifierTest;
import br.ufsc.lehmann.classifier.MSMClassifierTest;

public class MultipleThresholdRunner {

	public static void main(String[] args) {
		JUnitCore junit = new JUnitCore();
		String folderPath = "C:\\Users\\uuario\\Desktop\\Lehmann\\ArtigoMSM_UFSC\\artigo-helper\\src\\main\\resources\\only-stops-dataset\\RAW_20180123";
		File folder = new File(folderPath);
		folder.mkdirs();
		
		for (int spatialThreshold = 0; spatialThreshold <= 300; spatialThreshold +=50) {
			for (int timeThreshold = 0; timeThreshold <= 6; timeThreshold+=1) {
				for (double proportionalTimeThreshold = 0.1; proportionalTimeThreshold <= 0.1; proportionalTimeThreshold += .2) {
					FileOutputStream out = null;
					try {
						File file = new File(folder
								+ String.format("\\RawTrajectoriesClassifierTest Thresholds-%dms %dhs.out",
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
						Thresholds.SLACK_TEMPORAL.setValue(timeThreshold * 60 * 60 * 1000);
						junit.run( 
								EDRClassifierTest.class,
								MSMClassifierTest.class, 
								LCSSClassifierTest.class 
//								SMSMTemporalDurationClassifierTest.class, 
//								SMSMDistanceClassifierTest.class,
//								SMSMEllipsesClassifierTest.class,  
//								SMSMEllipsesWithDistanceClassifierTest.class,
//								SMSMEllipsesWithTemporalDurationClassifierTest.class,
//								SMSMEllipsesWithDistanceAndTemporalDurationClassifierTest.class 
								);
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
