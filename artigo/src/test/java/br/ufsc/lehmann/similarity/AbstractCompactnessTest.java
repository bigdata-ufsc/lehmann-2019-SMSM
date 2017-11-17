package br.ufsc.lehmann.similarity;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import smile.math.Random;

@RunWith(Parameterized.class)
public abstract class AbstractCompactnessTest {
	private static final Random RANDOM = new Random(5);
    @Rule public TestName name = new TestName();

	private EnumProblem descriptor;
	private Problem problem;
	
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }

	public AbstractCompactnessTest(EnumProblem problemDescriptor) {
		descriptor = problemDescriptor;
		problem = problemDescriptor.problem(RANDOM);
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@Test
	public void oneClassCompactness() throws Exception {
		List<SemanticTrajectory> trajs = problem.data();
//		trajs = problem.balancedData();
		
		//
		//459330,175302,347277,564396,525373 (a->m/101)
		//374700,614527,73718,176231,771453 (a->m/280)
		//409260,484589,768378,418507,461179 (m->a/101)
		//431550,59000,801975,595185,983724 (m->a/280)
		
		trajs = trajs.stream()//
//				.filter(t -> t.length() > 6)//
				.filter(t -> Arrays.asList(
						172186,614059
						).contains(t.getTrajectoryId()))//
//				.filter(t -> Arrays.asList("mall to airport").contains(SanFranciscoCabDataReader.DIRECTION.getData(t, 0)))//
//				.filter(t -> Arrays.asList("MTABC_7094061-YODD4-YO_D4-Saturday-10", "MTA NYCT_KB_D4-Saturday-106300_M100_332").contains(t.getTrajectoryId()))//
				.sorted((o1, o2) -> ((Comparable) o1.getTrajectoryId()).compareTo(o2.getTrajectoryId()))//
				.collect(Collectors.toList());
		
		System.out.printf("%d trajectories\n", trajs.size());

		SemanticTrajectory[] trajsArray = trajs.toArray(new SemanticTrajectory[trajs.size()]);
		Table<SemanticTrajectory, SemanticTrajectory, Double> allDistances = ArrayTable.create(trajs, trajs);
		IMeasureDistance<SemanticTrajectory> measureDistance = measurer(problem);
		Semantic semantic = problem.discriminator();
		for (int i = 0; i < trajsArray.length; i++) {
			for (int j = i; j < trajsArray.length; j++) {
				double distance = measureDistance.distance(trajsArray[i], trajsArray[j]);
				Object classData = semantic.getData(trajsArray[i], 0);
				Object otherClassData = semantic.getData(trajsArray[j], 0);
				if(!(Objects.equals(classData, otherClassData) && distance < 0.15)) {
					distance = measureDistance.distance(trajsArray[i], trajsArray[j]);
				}
				allDistances.put(trajsArray[i], trajsArray[j], distance);
				allDistances.put(trajsArray[j], trajsArray[i], distance);
			}
		}
		for (int i = 0; i < trajsArray.length; i++) {
			Object classData = semantic.getData(trajsArray[i], 0);
			List<Map.Entry<SemanticTrajectory, Double>> rows = allDistances.row(trajsArray[i]).entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
			assertEquals(0.0, rows.get(0).getValue(), 0.0000001);
			Object bestMatchClass = problem.discriminator().getData(rows.get(1).getKey(), 0);
			if(!Objects.equals(classData, bestMatchClass)) {
				System.out.print(trajsArray[i].getTrajectoryId() + " -> " + classData + ": ");
				System.out.printf("Best match: %d[%s](%.3f)\n", 
						rows.get(1).getKey().getTrajectoryId(),
						bestMatchClass.toString(),
						rows.get(1).getValue());
			}
			
			boolean pureDistanceLine = true;
			int j = 1;
			for (; j < rows.size() || !pureDistanceLine; j++) {
				Object otherClassData = semantic.getData(rows.get(j).getKey(), 0);
				if(!Objects.equals(classData, otherClassData)) {
					pureDistanceLine = false;
					break;
				}
			}
			j++;
			for (; j < rows.size(); j++) {
				Object otherClassData = semantic.getData(rows.get(j).getKey(), 0);
				if(Objects.equals(classData, otherClassData)) {
					//fail("Non pure distance vector");
				}
			}
		}
//		Multimap<Object, SemanticTrajectory> classifiedTrajectories = MultimapBuilder.linkedHashKeys().arrayListValues().build();
//		for (SemanticTrajectory traj : trajs) {
//			classifiedTrajectories.put(semantic.getData(traj, 0), traj);
//		}
//		Map<Object, Collection<SemanticTrajectory>> classAsMap = classifiedTrajectories.asMap();
//		for (Map.Entry<Object, Collection<SemanticTrajectory>> entry : classAsMap.entrySet()) {
//			
//		}
	}
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
}
