package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;
import smile.math.Random;

public class InvolvesPrecision10 {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
//		EnumProblem prob = EnumProblem.INVOLVES;
//		Problem problem = prob.problem(new Random());
//		List<SemanticTrajectory> data = problem.data();
//		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
////		AbstractClassifierTest t = new UMSClassifierTest(prob);
//		AbstractClassifierTest t = new SMSMEllipsesClassifierTest(prob);
//		
//		TrajectorySimilarityCalculator<SemanticTrajectory> classifier = (TrajectorySimilarityCalculator<SemanticTrajectory>) t.measurer(problem);
		Random r = new Random();
		
		ExecutionPOJO execution = new Gson().fromJson(new FileReader("./src/test/resources/executions/SMSM_precision@10.test"), ExecutionPOJO.class);
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		IDataReader dataReader = Datasets.createDataset(dataset);
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);
		List<SemanticTrajectory> data = dataReader.read();
		BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		
		Multimap<Integer, SemanticTrajectory> trajs = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < allData.length; i++) {
			trajs.put(InvolvesDatabaseReader.USER_ID.getData(allData[i], 0), allData[i]);
		}
		List<SemanticTrajectory> bestTrajectories = trajs.keySet().stream().map((key) -> trajs.get(key).stream().max(new Comparator<SemanticTrajectory>() {

			@Override
			public int compare(SemanticTrajectory o1, SemanticTrajectory o2) {
				double proportionO1 = movesDuration(o1) / stopsDuration(o1);
				double proportionO2 = movesDuration(o2) / stopsDuration(o2);
				return Double.compare(proportionO1, proportionO2);
			}

			private double movesDuration(SemanticTrajectory o1) {
				double ret = 0.0;
				List<Move> allMoves = new ArrayList<>();
				for (int i = 0; i < o1.length(); i++) {
					Stop stop = InvolvesDatabaseReader.STOP_NAME_SEMANTIC.getData(o1, i);
					if(stop != null) {
						if(stop.getPreviousMove() != null && !allMoves.contains(stop.getPreviousMove())) {
							allMoves.add(stop.getPreviousMove());
						}
						if(stop.getNextMove() != null && !allMoves.contains(stop.getNextMove())) {
							allMoves.add(stop.getNextMove());
						}
					}
				}
				for (Move move : allMoves) {
					ret += move.getEndTime() - move.getStartTime();
				}
				return ret;
			}

			private double stopsDuration(SemanticTrajectory o1) {
				double ret = 0.0;
				for (int i = 0; i < o1.length(); i++) {
					Stop stop = InvolvesDatabaseReader.STOP_NAME_SEMANTIC.getData(o1, i);
					if(stop != null) {
						ret += stop.getEndTime() - stop.getStartTime();
					}
				}
				return ret;
			}
		}).get()).collect(Collectors.toList());
		Map<SemanticTrajectory, List<SemanticTrajectory>> mostSimilarTrajs = 
				bestTrajectories.stream()//
					.map((traj) -> new Object[] {traj, findMostSimilar(traj, allData, similarityCalculator, 5 + 1)})//
					.collect(Collectors.toMap((value) -> (SemanticTrajectory) value[0], value -> (List<SemanticTrajectory>) value[1]));
		
		List<SemanticTrajectory> keys = mostSimilarTrajs.keySet().stream().sorted(new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				return Double.compare(InvolvesDatabaseReader.USER_ID.getData((SemanticTrajectory) o1, 0), InvolvesDatabaseReader.USER_ID.getData((SemanticTrajectory) o2, 0));
			}
		}).collect(Collectors.toList());
		int errorsCount = 0;
		for (SemanticTrajectory key : keys) {
			List<SemanticTrajectory> entries = mostSimilarTrajs.get(key);
			Integer keyUserId = InvolvesDatabaseReader.USER_ID.getData(key, 0);
			String msg = String.format("colab = %d, dimensao_data = %d: ", 
					keyUserId, 
					InvolvesDatabaseReader.DIMENSAO_DATA.getData(key, 0));
			System.out.println(msg);
			for (SemanticTrajectory t : entries) {
				Integer tUserId = InvolvesDatabaseReader.USER_ID.getData(t, 0);
				System.out.printf("\tcolab = %d, dimensao_data = %d, distancia = %.4f\n",
						tUserId, 
						InvolvesDatabaseReader.DIMENSAO_DATA.getData(t, 0),
						1 - similarityCalculator.getSimilarity(key, t));
				if(!keyUserId.equals(tUserId)) {
					errorsCount++;
				}
			}
		}
		System.out.println("Mislabeled trajectories: " + errorsCount);
	}

	private static List<SemanticTrajectory> findMostSimilar(SemanticTrajectory traj, SemanticTrajectory[] allData, TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator, int n) {
		Map<SemanticTrajectory, Double> ret = new HashMap<>();
		for (SemanticTrajectory trajectory : allData) {
			double similarity = similarityCalculator.getSimilarity(traj, trajectory);
			ret.put(trajectory, 1 - similarity);
		}
		return ret.entrySet()//
				.stream()//
				.sorted(Comparator.comparing(Map.Entry::getValue))//
				.limit(n)//
				.map((entry) -> entry.getKey())//
				.collect(Collectors.toList());
	}
}
