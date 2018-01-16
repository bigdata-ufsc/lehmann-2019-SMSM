package br.ufsc.lehmann.classifier;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	MSMClassifierTest.class, 
	//DTWClassifierTest.class, //
	EDRClassifierTest.class, 
	//ERPClassifierTest.class, 
	LCSSClassifierTest.class, MSTPClassifierTest.class,//
	//HCSSClassifierTest.class, 
	CVTIClassifierTest.class, wDFClassifierTest.class, 
//	SWALEClassifierTest.class, LiuSchneiderClassifierTest.class, 
	DTWaClassifierTest.class, /*MTMClassifierTest.class*/
//	UMSClassifierTest.class,
//	SMSMTemporalDurationClassifierTest.class, 
//	SMSMDistanceClassifierTest.class,
	SMSMEllipsesClassifierTest.class,  
//	SMSMEllipsesWithDistanceClassifierTest.class,
//	SMSMEllipsesWithTemporalDurationClassifierTest.class,
//	SMSMEllipsesWithDistanceAndTemporalDurationClassifierTest.class, 
	})
public class AllClassifierTests {

}
