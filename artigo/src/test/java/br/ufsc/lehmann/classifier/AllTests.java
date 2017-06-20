package br.ufsc.lehmann.classifier;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DTWClassifierTest.class, DTWaClassifierTest.class, EDRClassifierTest.class, ERPClassifierTest.class, LCSSClassifierTest.class,
		LiuSchneiderClassifierTest.class, MSMClassifierTest.class, MSTPClassifierTest.class, MTMClassifierTest.class })
public class AllTests {

}
