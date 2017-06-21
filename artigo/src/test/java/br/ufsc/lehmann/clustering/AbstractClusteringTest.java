package br.ufsc.lehmann.clustering;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity.CompleteLinkDissimilarity;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.AdjustedRandIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.DunnIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.MaxDistance;

public abstract class AbstractClusteringTest {
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
	
	@Test
	public void selfClassification() throws Exception {
		NElementProblem problem = new NElementProblem(2);
		TestClusteringExecutor executor = new TestClusteringExecutor(2);

		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		ClusteringResult result = null;
		try {
			result = executor.cluster(problem, classifier);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int[] clusterLabel = result.getClusterLabel();
		assertTrue(Arrays.asList(0, 1).containsAll(Arrays.stream(clusterLabel).boxed().collect(Collectors.toList())));
	}

	@Test
	public void simpleClassification() throws Exception {
		TestClusteringExecutor executor = new TestClusteringExecutor(5);
		NElementProblem problem = new NElementProblem(15);
		List<SemanticTrajectory> data = problem.data();
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		ClusteringResult result = null;
		try {
			result = executor.cluster(problem, classifier);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		AdjustedRandIndex<String> randIndex = new AdjustedRandIndex<String>();
		double value = randIndex.evaluate(result.getClusterLabel(), new Trajectories<>(data, problem.discriminator()));
		assertEquals(0.0, value, 0.00001);
		DunnIndex<String> dunnIndex = new DunnIndex<>(new MaxDistance(classifier), new CompleteLinkDissimilarity(classifier));
		value = dunnIndex.evaluate(result.getClusterLabel(), new Trajectories<>(data, problem.discriminator()));
		assertEquals(0.0, value, 0.00001);
	}
}
