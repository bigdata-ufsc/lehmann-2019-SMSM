package br.ufsc.lehmann.clustering;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	MSMClusteringTest.class, 
	DTWClusteringTest.class, 
	EDRClusteringTest.class, ERPClusteringTest.class, 
	LCSSClusteringTest.class, HCSSClusteringTest.class, 
	MSTPClusteringTest.class,
	CVTIClusteringTest.class, 
	LiuSchneiderClusteringTest.class/*, MTMClusteringTest.class*/, 
	H_MSM_StopMove_EllipsesClusteringTest.class, 
	DTWaClusteringTest.class })
public class AllClusteringTests {

}
