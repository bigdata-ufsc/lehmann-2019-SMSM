package br.ufsc.lehmann.classifier;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MSMClassifierTest.class, MSMMoveClassifierTest.class, MSMMovePointsClassifierTest.class, DTWClassifierTest.class, DTWaClassifierTest.class, //
	EDRClassifierTest.class, ERPClassifierTest.class, LCSSClassifierTest.class, MSTPClassifierTest.class,//
	HCSSClassifierTest.class, CVTIClassifierTest.class, wDFClassifierTest.class, SWALEClassifierTest.class, 
	LiuSchneiderClassifierTest.class, /*MTMClassifierTest.class*/ })
public class AllClassifierTests {

}
