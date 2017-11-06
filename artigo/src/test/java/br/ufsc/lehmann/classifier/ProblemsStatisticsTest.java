package br.ufsc.lehmann.classifier;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import smile.math.Random;

@RunWith(Parameterized.class)
public class ProblemsStatisticsTest {

	@Rule public TestName name = new TestName();
	private Random random;
	private Problem problem;
    
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }
	
	public ProblemsStatisticsTest(EnumProblem problemDescriptor) {
		random = new Random(5);
		problem = problemDescriptor.problem(random);
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@Test
	public void classesStatistics() throws Exception {
		List<SemanticTrajectory> trajs = problem.data();
		Map<Object, Long> countedDup =  trajs
		        .stream().collect(Collectors.groupingBy(c -> problem.discriminator().getData(c, 0), Collectors.counting()));
        System.out.println(countedDup);
	}
	
	@Test
	public void trajectories() throws Exception {
		List<SemanticTrajectory> trajs = problem.data();
		Map<Object, List<SemanticTrajectory>> countedDup =  trajs
		        .stream().collect(Collectors.groupingBy(c -> problem.discriminator().getData(c, 0)));
		for (Iterator iterator = countedDup.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Object, List<SemanticTrajectory>> user = (Map.Entry<Object, List<SemanticTrajectory>>) iterator.next();
			System.out.println("User id = " + user.getKey());
			for (SemanticTrajectory semanticTrajectory : user.getValue()) {
				for (int i = 0; i < semanticTrajectory.length(); i++) {
					Stop data = PisaDataReader.STOP_NAME_SEMANTIC.getData(semanticTrajectory, i);
					if(data != null) {
						System.out.print(data.getStopName());
						if(i + 1 < semanticTrajectory.length()) {
							System.out.print(" -> ");
						} else {
							System.out.println("");
						}
					}
				}
			}
		}
	}

}
