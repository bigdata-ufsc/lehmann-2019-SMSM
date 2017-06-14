package br.ufsc.lehmann.classifier;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DTWaClassifierTest.class, EDRClassifierTest.class, ERPClassifierTest.class, LCSSClassifierTest.class,
		LiuSchneiderClassifierTest.class, MSMClassifierTest.class })
public class AllTests {

}
