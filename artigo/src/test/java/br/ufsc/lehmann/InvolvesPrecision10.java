package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
				for (int i = 0; i < o1.length(); i++) {
					Move move = InvolvesDatabaseReader.MOVE_TEMPORAL_DURATION_SEMANTIC.getData(o1, i);
					if(move != null) {
						ret += move.getEndTime() - move.getStartTime();
					}
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
					.map((traj) -> new Object[] {traj, find10MostSimilar(traj, allData, similarityCalculator)})//
					.collect(Collectors.toMap((value) -> (SemanticTrajectory) value[0], value -> (List<SemanticTrajectory>) value[1]));
		
		for (Map.Entry<SemanticTrajectory, List<SemanticTrajectory>> entry : mostSimilarTrajs.entrySet()) {
			String msg = String.format("colab = %d, dimensao_data = %d: ", 
					InvolvesDatabaseReader.USER_ID.getData(entry.getKey(), 0), 
					InvolvesDatabaseReader.DIMENSAO_DATA.getData(entry.getKey(), 0));
			System.out.println(msg);
			for (SemanticTrajectory t : entry.getValue()) {
				System.out.printf("\tcolab = %d, dimensao_data = %d, distancia = %.4f\n",
						InvolvesDatabaseReader.USER_ID.getData(t, 0), 
						InvolvesDatabaseReader.DIMENSAO_DATA.getData(t, 0),
						1 - similarityCalculator.getSimilarity(entry.getKey(), t));
			}
		}
		
//		Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) similarityCalculator, r);
//
//		double[] precisionAtRecall = validation.precisionAtRecall(similarityCalculator, allData, 10);
//		System.out.printf("Precision@recall(%d): %s\n", 10, ArrayUtils.toString(precisionAtRecall, "0.0"));
//		double auc = AUC.precisionAtRecall(precisionAtRecall);
//		System.out.printf("AUC: %.4f\n", auc);
	}

	private static List<SemanticTrajectory> find10MostSimilar(SemanticTrajectory traj, SemanticTrajectory[] allData, TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator) {
		Map<SemanticTrajectory, Double> ret = new HashMap<>();
		for (SemanticTrajectory trajectory : allData) {
			double similarity = similarityCalculator.getSimilarity(traj, trajectory);
			ret.put(trajectory, 1 - similarity);
		}
		return ret.entrySet()//
				.stream()//
				.sorted(Comparator.comparing(Map.Entry::getValue))//
				.limit(10)//
				.map((entry) -> entry.getKey())//
				.collect(Collectors.toList());
	}
}
