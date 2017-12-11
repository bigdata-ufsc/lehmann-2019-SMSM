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
	CVTIClassifierTest.class, wDFClassifierTest.class, SWALEClassifierTest.class, 
	//LiuSchneiderClassifierTest.class, 
	DTWaClassifierTest.class, /*MTMClassifierTest.class*/
//	SMSMTemporalDurationClassifierTest.class, SMSMDistanceClassifierTest.class, SMSMAngleClassifierTest.class, SMSMEllipsesClassifierTest.class, SMSMPointsClassifierTest.class,  
//	H_MSM_TemporalDurationClassifierTest.class, H_MSM_DistanceClassifierTest.class, H_MSM_AngleClassifierTest.class, H_MSM_EllipsesClassifierTest.class, H_MSM_PointsClassifierTest.class,
	//H_MSM_StopMove_TemporalDurationClassifierTest.class, H_MSM_StopMove_DistanceClassifierTest.class, H_MSM_StopMove_AngleClassifierTest.class, 
	H_MSM_StopMove_EllipsesClassifierTest.class, 
	//H_MSM_StopMove_PointsClassifierTest.class, 
	})
public class AllClassifierTests {

}
