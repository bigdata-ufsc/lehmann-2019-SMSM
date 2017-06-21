package br.ufsc.lehmann.clustering;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DTWClusteringTest.class, DTWaClusteringTest.class, EDRClusteringTest.class, ERPClusteringTest.class, LCSSClusteringTest.class,
		LiuSchneiderClusteringTest.class, MSMClusteringTest.class, MSTPClusteringTest.class, MTMClusteringTest.class })
public class AllClusteringTests {

}
