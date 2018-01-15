package br.ufsc.lehmann;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class GeolifePathFinder {
	

	public static void main(String[] args) {
		int[] A = {18531,19388,21204,21311,21485,21713,22521};
		int[] B = {15474,16302,20487,21675,21838,21882,24635,33521};
		int[] C = {12560,12619,12724,12764,12952,13021,13064,13081,13253,13273,13637,13653,13671,13752,13845,13862,13995,14240,14281,14316,14347,14373,14446,14533,14568,15010,15217,17890,18510,18571,18781,20330,20651,20910,21176,21638,22193,23412,23668,35843,4126,5851};
		int[] D = {14199,17936,18611,18630,18767,18967,19009,19173,19207,19237,19554,21820,27886,45188};
		int[] E = {12604,12804,12978,13035,13127,13158,13201,13206,13357,13390,13423,13474,13629,13664,13742,13839,14063,14165,14173,14255,14340,14946,15202,15244,15384,16110,16324,16635,16669,16721,16760,16796,16868,16915,16987,17011,17114,17124,17146,17180,17204,17268,17321,17447,17493,17636,17733,17790,17828,17918,17950,18050,18084,18126,18191,18404,18434,18463,18483,18503,18517,18556,18579,18594,18601,18621,18691,18704,18715,18738,18754,18791,18792,18829,18950,18990,19057,19094,19165,19186,19283,19301,19339,19351,19361,19373,19380,19399,19401,19430,19453,19491,19561,19569,19587,19596,19608,19616,19628,19786,19795,19834,19852,19953,19982,20010,20036,20072,20157,20297,20308,20412,20420,20427,20453,20467,20480,20549,20598,20613,20637,20652,20751,20810,20833,20848,20920,20929,20934,20939,20956,20968,20983,20997,21003,21007,21015,21019,21039,21073,21086,21147,21155,21199,21211,21220,21230,21236,21305,21308,21503,21587,21639,21708,21736,21741,21745,21814,21848,21854,21882,21891,21901,21937,21954,21955,22025,22034,22046,22053,22065,22072,22099,22105,22130,22161,22228,22253,22325,22375,22386,22409,22415,22473,22488,22494,22505,22526,22578,22583,22872,22893,22919,22927,23035,23386,23399,23486,23500,23513,23544,23558,23594,23632,23726,23805,23845,23862,23931,23941,23950,23973,23980,23995,23997,23998,24018,24037,24066,24084,24215,24221,24241,24258,24306,24499,24534,24551,2460,24709,24838,24941,24987,25003,25011,25095,25155,25218,25285,25294,25534,27159,28499,2925,30243,32077,32089,32326,32447,32468,32507,32540,32665,32775,32781,32813,32951,32994,33028,33447,33492,33680,33701,33706,33994,34019,4034,40354,41169,41210,4227,4322,45069,45080,45684,45719,5517};
		int[] F = {19963,21485,22423,22506,22549,25330,25421,25490,37384};
		
		Map<String, LongAdder> caminhos = new HashMap<>();
		
		GeolifeUniversitySubProblem problem = new GeolifeUniversitySubProblem(GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, true);
		List<SemanticTrajectory> data = problem.data();
		System.out.print("Sem caminho: ");
		for (SemanticTrajectory semanticTrajectory : data) {
			List<String> pois = new ArrayList<>();
			for (int j = 0; j < semanticTrajectory.length(); j++) {
				Stop stop = GeolifeUniversityDataReader.STOP_REGION_SEMANTIC.getData(semanticTrajectory, j);
				if(stop != null) {
					pois.add(stop.getStopName());
				}
			}
			String classData = "";
			if(ArrayUtils.contains(A, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> A";
			} else if(ArrayUtils.contains(B, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> B";
			} else if(ArrayUtils.contains(C, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> C";
			} else if(ArrayUtils.contains(D, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> D";
			} else if(ArrayUtils.contains(E, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> E";
			} else if(ArrayUtils.contains(F, (Integer) semanticTrajectory.getTrajectoryId())) {
				classData = GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0) + " -> F";
			} else {
				System.out.printf("%d,", semanticTrajectory.getTrajectoryId());
				continue;
			}
			caminhos.computeIfAbsent(classData, (t) -> new LongAdder()).increment();
//			if(GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0).equals("Microsoft-Dormitory")) {
//				System.out.printf("%d - %s\n", semanticTrajectory.getTrajectoryId(), pois.toString());
//			}
//			if(GeolifeUniversityDataReader.DIRECTION.getData(semanticTrajectory, 0).equals("Dormitory-Microsoft")) {
//				System.out.printf("%d - %s\n", semanticTrajectory.getTrajectoryId(), pois.toString());
//			}
		}
		System.out.println();
		for (Iterator iterator = caminhos.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, LongAdder> entry = (Map.Entry<String, LongAdder>) iterator.next();
			System.out.println(entry.getKey() + " = " + entry.getValue().intValue());
		}
	}
}
