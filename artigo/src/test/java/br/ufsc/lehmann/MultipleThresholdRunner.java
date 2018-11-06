package br.ufsc.lehmann;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.runner.JUnitCore;

import br.ufsc.lehmann.classifier.CVTIClassifierTest;
import br.ufsc.lehmann.classifier.DTWaClassifierTest;
import br.ufsc.lehmann.classifier.EDRClassifierTest;
import br.ufsc.lehmann.classifier.LCSSClassifierTest;
import br.ufsc.lehmann.classifier.MSMClassifierTest;
import br.ufsc.lehmann.classifier.MSTPClassifierTest;
import br.ufsc.lehmann.classifier.SMSMEllipsesClassifierTest;
import br.ufsc.lehmann.classifier.UMSClassifierTest;
import br.ufsc.lehmann.classifier.wDFClassifierTest;

public class MultipleThresholdRunner {

	public static void main(String[] args) {
		JUnitCore junit = new JUnitCore();
		String folderPath = "../artigo-helper/src/main/resources/only-stops-dataset/Semantics_20181104";
		File folder = new File(folderPath);
		folder.mkdirs();
		
		for (int spatialThreshold = 100; spatialThreshold <= 100; spatialThreshold +=50) {
//			for (int timeThreshold = 0; timeThreshold <= 6; timeThreshold+=1) {
//				for (double proportionalTimeThreshold = 0.0; proportionalTimeThreshold <= 1.0; proportionalTimeThreshold += .01) {
					FileOutputStream out = null;
					PrintStream stream = null;
					try {
//						File file = new File(folder + 
//								String.format("/Thresholds_ClassifierTest Thresholds-%dm.out",
//										spatialThreshold));
//						if(file.exists()) {
//							file.delete();
//						}
						//file.createNewFile();
//						out = new FileOutputStream(file);
//						stream = new PrintStream(new BufferedOutputStream(out), true);
//						System.setOut(stream);
//						Thresholds.MOVE_INNER_POINTS_PERC.setValue(proportionalTimeThreshold);
						Thresholds.STOP_CENTROID_EUCLIDEAN.setValue(spatialThreshold);
						Thresholds.STOP_CENTROID_LATLON.setValue(spatialThreshold);
						Thresholds.SPATIAL_EUCLIDEAN.setValue(spatialThreshold);
						Thresholds.SPATIAL_LATLON.setValue(spatialThreshold);
//						Thresholds.TEMPORAL.setValue(proportionalTimeThreshold);
//						Thresholds.SLACK_TEMPORAL.setValue(timeThreshold * 60 * 60 * 1000);
						junit.run(  
//								wDFClassifierTest.class,
								EDRClassifierTest.class,
//								MSMClassifierTest.class, 
								LCSSClassifierTest.class,
//								CVTIClassifierTest.class,
//								MSTPClassifierTest.class,
//								DTWaClassifierTest.class,
//								SMSMTemporalDurationClassifierTest.class, 
//								SMSMDistanceClassifierTest.class,
//								SMSMEllipsesClassifierTest.class
//								SMSMEllipsesWithDistanceClassifierTest.class,
//								SMSMEllipsesWithTemporalDurationClassifierTest.class,
//								SMSMEllipsesWithDistanceAndTemporalDurationClassifierTest.class
								UMSClassifierTest.class
								);
//					} catch (IOException e) {
//						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.flush();
								stream.flush();
								out.close();
								stream.close();
							} catch (IOException e) {
							}
						}
//					}
//				}
			}
		}
		
	}
	
}
