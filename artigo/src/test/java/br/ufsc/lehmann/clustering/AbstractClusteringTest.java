package br.ufsc.lehmann.clustering;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Silhouette;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Silhouettes;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity.CompleteLinkDissimilarity;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.AdjustedRandIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.DunnIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.MaxDistance;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix;
import smile.math.Random;

@RunWith(Parameterized.class)
public abstract class AbstractClusteringTest {
	
	private static final Random RANDOM = new Random(5);
	
    @Rule public TestName name = new TestName();
	private Multimap<String, String> measureFailures = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();

	private EnumProblem descriptor;
	private Problem problem;
	
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }
    
	public AbstractClusteringTest(EnumProblem problemDescriptor) {
		descriptor = problemDescriptor;
		problem = problemDescriptor.problem(RANDOM);
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@After
	public void after() {
		if(!measureFailures.isEmpty()) {
			StringWriter sw = new StringWriter();
			for (Map.Entry<String, Collection<String>> entry : measureFailures.asMap().entrySet()) {
				sw.append(entry.getKey()).append(" - [");
				Collection<String> value = entry.getValue();
				for (Iterator iterator = value.iterator(); iterator.hasNext();) {
					String message = (String) iterator.next();
					sw.append(message);
					if(iterator.hasNext()) {
						sw.append(", ");
					}
				}
				sw.append("]\n");
			}
			Assert.fail(sw.toString());
		}
	}

	@Test
	public void simpleClusterizationBySimilarityMeasure() throws Exception {
		HierarchicalClusteringDistanceBetweenTrajectoriesExecutor executor = new HierarchicalClusteringDistanceBetweenTrajectoriesExecutor(descriptor.numClasses());
//		List<SemanticTrajectory> data = problem.data();
		List<SemanticTrajectory> data = problem.balancedData();
		IMeasureDistance<SemanticTrajectory> measurer = measurer(problem);
		SemanticTrajectory[] training = data.toArray(new SemanticTrajectory[data.size()]);
		double[][] distances = new double[training.length][training.length];
		for (int i = 0; i < training.length; i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
				distances[finalI][j] = measurer.distance(training[finalI], training[j]);
				distances[j][finalI] = distances[finalI][j];
			});
		}
		ClusteringResult result = null;
		try {
			result = executor.cluster(distances, training, measurer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		AdjustedRandIndex<String> randIndex = new AdjustedRandIndex<String>();
		Trajectories trajectories = new Trajectories<>(data, problem.discriminator());
		int[] clusterLabel = result.getClusterLabel();
		double value = randIndex.evaluate(clusterLabel, trajectories);
		assertMeasure("AdjustedRandIndex", 0.0, value, 0.01);
		DistanceMatrix<SemanticTrajectory> matrix = new DistanceMatrix<>(data, distances);
		DunnIndex<String> dunnIndex = new DunnIndex<>(new MaxDistance(matrix), new CompleteLinkDissimilarity(matrix));
		value = dunnIndex.evaluate(clusterLabel, trajectories);
		assertMeasure("DunnIndex", 0.0, value, 0.01);
		Silhouettes silhouette = Silhouette.calculate(distances, clusterLabel);
		for (int i = 0; i < result.getClusteres().size(); i++) {
			value = silhouette.getSilhouette(i);
			assertMeasure("Silhouette (cluster " + i + ")", String.format("Expected a silhouette index greater than 0.0 but was %.2f", value), 0.0 <= value);
		}
	}
	
	public void assertMeasure(String measure, double expected, double actual, double delta) {
		if(Math.abs(expected - actual) > delta) {
			measureFailures.put(measure, "Expected was " + expected + " but actual is " + actual);
		}
	}
	
	public void assertMeasure(String measure, String message, double expected, double actual, double delta) {
		if(Math.abs(expected - actual) > delta) {
			measureFailures.put(measure, message);
		}
	}
	
	public void assertMeasure(String measure, boolean test) {
		if(!test) {
			measureFailures.put(measure, "Expected true but actual is false");
		}
	}
	
	public void assertMeasure(String measure, String message, boolean test) {
		if(!test) {
			measureFailures.put(measure, message);
		}
	}
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
}
