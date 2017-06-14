package br.ufsc.lehmann;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;

public class MSMClassifierTest {

	private static NearestNeighbour<SemanticTrajectory> nn;
	private static List<SemanticTrajectory> trajectories;
	private static List<SemanticTrajectory> training;
	private static List<KNNTestData<SemanticTrajectory, Object>> testing;

	@BeforeClass
	public static void setupClass() throws IOException, InterruptedException {
		trajectories = new BikeTestDataReader().read();
		training = trajectories.subList(0, (int) (trajectories.size() * 2.0/3) - 1);
		testing = trajectories.subList((int) (trajectories.size() * 2.0/3), trajectories.size() - 1).stream().map((SemanticTrajectory st) -> new KNNTestData<SemanticTrajectory, Object>(st, st.getDimensionData(7, 0))).collect(Collectors.toList());
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : training) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, traj.getDimensionData(7, 0)));
		}
		nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(training.size(), 3), new MSMClassifier(), false);
	}
	
	@Test
	public void testName() throws Exception {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (KNNTestData<SemanticTrajectory, Object> st : testing) {
			Object[] classified = (Object[]) nn.classify(new DataEntry<SemanticTrajectory>(st.getData(), "descubra"));
			System.out.println(Arrays.toString(classified));
			stats.addValue(Arrays.equals((Object[]) st.getKlazz(), classified) ? 1.0 : 2.0);
		}
		System.out.println(stats.toString());
	}
}
