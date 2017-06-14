package br.ufsc.lehmann.classifier;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.TestClassificationExecutor;
import br.ufsc.lehmann.TestProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public abstract class AbstractClassifierTest {
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);

	@Test
	public void simpleClassification() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		stats.put("0", new DescriptiveStatistics());
		stats.put("1", new DescriptiveStatistics());
		stats.put("2", new DescriptiveStatistics());
		stats.put("3", new DescriptiveStatistics());
		stats.put("4", new DescriptiveStatistics());
		TestClassificationExecutor executor = new TestClassificationExecutor(stats);
		TestProblem problem = new TestProblem();
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		try {
			executor.classify(problem, classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(1.0, stats.get("0").getMean(), 0.000001);
		assertEquals(2, stats.get("0").getValues().length);
		assertEquals(1.0, stats.get("1").getMean(), 0.000001);
		assertEquals(2, stats.get("1").getValues().length);
		assertEquals(1.0, stats.get("2").getMean(), 0.000001);
		assertEquals(2, stats.get("2").getValues().length);
		assertEquals(1.0, stats.get("3").getMean(), 0.000001);
		assertEquals(2, stats.get("3").getValues().length);
		assertEquals(1.0, stats.get("4").getMean(), 0.000001);
		assertEquals(2, stats.get("4").getValues().length);
	}
}
