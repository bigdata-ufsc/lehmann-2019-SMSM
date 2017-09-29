package br.ufsc.lehmann.classifier;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
//	MSMMoveDistanceClassifierTest.class, MSMMoveAngleClassifierTest.class,  
//	MSMClassifierTest.class, 
//	DTWClassifierTest.class, //
//	EDRClassifierTest.class, ERPClassifierTest.class, LCSSClassifierTest.class, MSTPClassifierTest.class,//
//	HCSSClassifierTest.class, CVTIClassifierTest.class, wDFClassifierTest.class, SWALEClassifierTest.class, 
//	LiuSchneiderClassifierTest.class, 
//	MSMMoveEllipsesClassifierTest.class, 
	MSMMovePointsClassifierTest.class, 
	DTWaClassifierTest.class, /*MTMClassifierTest.class*/
	})
public class AllClassifierTests {

}
