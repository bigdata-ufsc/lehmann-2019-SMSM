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
		trajs = problem.balancedData();
		//944553,768051,130346,360156 (280/mall to airport)
		//733677 (280/airport to mall)
		//541571,740318,717577,872545 (101/mall to airport)
		//331711,754915,740318,328903,755692 (101/airport to mall)
		
		//
		//459330,175302,347277,564396,525373 (a->m/101)
		//374700,614527,73718,176231,771453 (a->m/280)
		//409260,484589,768378,418507,461179 (m->a/101)
		//431550,59000,801975,595185,983724 (m->a/280)
		
		trajs = trajs.stream()//
				.filter(t -> Arrays.asList(
//						459330,175302,347277,564396,525373, //(a->m/101)
//						374700,614527,176231,73718,771453 //(a->m/280)
//						409260,484589,768378,418507,461179, //(m->a/101)
//						431550,59000,801975,595185,983724 //(m->a/280)
//						438507,						221720,						207926,						754915,
//						671867,						740318,						969847,						456659,
//						172186,						687355,						331711,						345591,
//						351344,						52546,						351391,						311081,
//						525373,						846995,						508323,						91493,
//						347277,						325766,						966146,						89144,
//						564396,						731217,						763218,						260461,
//						408861,						408385,						495702,						508631,
//						488350,						300359,						519257,						755692,
//						459330,						24686,						85432,
//						16427,						398147,						819009,						175302,
//						689815,						577772,						724312,						106879,
//						672714,						109402,						328903,						253186,
//						940828
						768051,940828
						).contains(t.getTrajectoryId()))//
				.sorted((o1, o2) -> ((Comparable) o1.getTrajectoryId()).compareTo(o2.getTrajectoryId()))//
				.collect(Collectors.toList());

		SemanticTrajectory[] trajsArray = trajs.toArray(new SemanticTrajectory[trajs.size()]);
		Table<SemanticTrajectory, SemanticTrajectory, Double> allDistances = ArrayTable.create(trajs, trajs);
		IMeasureDistance<SemanticTrajectory> measureDistance = measurer(problem);
		Semantic semantic = problem.discriminator();
		for (int i = 0; i < trajsArray.length; i++) {
			for (int j = i; j < trajsArray.length; j++) {
				double distance = measureDistance.distance(trajsArray[i], trajsArray[j]);
//				Object classData = semantic.getData(trajsArray[i], 0);
//				Object otherClassData = semantic.getData(trajsArray[j], 0);
//				if(!(Objects.equals(classData, otherClassData) && distance > 0.5)) {
//					distance = 0.0;
//				}
				allDistances.put(trajsArray[i], trajsArray[j], distance);
				allDistances.put(trajsArray[j], trajsArray[i], distance);
			}
		}
		for (int i = 0; i < trajsArray.length; i++) {
			Object classData = semantic.getData(trajsArray[i], 0);
			List<Map.Entry<SemanticTrajectory, Double>> rows = allDistances.row(trajsArray[i]).entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
			System.out.print(trajsArray[i].getTrajectoryId() + " -> " + classData + ": ");
			for (int j = 0; j < rows.size(); j++) {
				System.out.print(semantic.getData(rows.get(j).getKey(), 0) + "(" + rows.get(j).getValue() + ") < ");
			}
			System.out.print('\n');
			assertEquals(0.0, rows.get(0).getValue(), 0.0000001);
			//assertEquals(rows.get(0).getKey(), trajsArray[i]);
			
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
