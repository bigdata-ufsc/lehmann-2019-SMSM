package br.ufsc.lehmann.clustering;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DTWClusteringTest.class, EDRClusteringTest.class, ERPClusteringTest.class, LCSSClusteringTest.class, MSMClusteringTest.class, MSTPClusteringTest.class,
	HCSSClusteringTest.class, CVTIClusteringTest.class, LiuSchneiderClusteringTest.class/*, MTMClusteringTest.class*/, MSMMoveClusteringTest.class, DTWaClusteringTest.class })
public class AllClusteringTests {

}
