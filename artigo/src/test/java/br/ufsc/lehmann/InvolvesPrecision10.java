package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.InvolvesRecoverTrajectoryStats.GroundtruthRanking;
import br.ufsc.lehmann.InvolvesRecoverTrajectoryStats.RankingPosition;
import br.ufsc.lehmann.metric.NDCG;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;

public class InvolvesPrecision10 {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ExecutionPOJO execution = new Gson().fromJson(new FileReader("./src/test/resources/executions/SMSMPartial_precision.test"), ExecutionPOJO.class);
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		IDataReader dataReader = Datasets.createDataset(dataset);
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);
		List<SemanticTrajectory> data = dataReader.read();
		BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		
		InvolvesRecoverTrajectoryStats recoveryStats = new InvolvesRecoverTrajectoryStats();
		List<SemanticTrajectory> bestTrajectories = recoveryStats.recoverBestTrajectories(allData);
		Map<Integer, GroundtruthRanking> ranking = recoveryStats.getRanking();
		Map<SemanticTrajectory, List<SemanticTrajectory>> mostSimilarTrajs = 
				bestTrajectories.stream()//
					.map((traj) -> new Object[] {traj, findMostSimilar(traj, allData, similarityCalculator)})//
					.collect(Collectors.toMap((value) -> (SemanticTrajectory) value[0], value -> (List<SemanticTrajectory>) value[1]));
		
		List<SemanticTrajectory> keys = mostSimilarTrajs.keySet().stream().sorted(new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				return Double.compare(InvolvesDatabaseReader.USER_ID.getData((SemanticTrajectory) o1, 0), InvolvesDatabaseReader.USER_ID.getData((SemanticTrajectory) o2, 0));
			}
		}).collect(Collectors.toList());
		int errorsCount = 0;
		double meanRankingError = 0.0;
		Map<String, DescriptiveStatistics> stats = new HashMap<>();
		for (SemanticTrajectory key : keys) {
			List<SemanticTrajectory> entries = mostSimilarTrajs.get(key);
			Integer keyUserId = InvolvesDatabaseReader.USER_ID.getData(key, 0);
			Integer keyDimensaoData = InvolvesDatabaseReader.DIMENSAO_DATA.getData(key, 0);
			String msg = String.format("colab = %d, dimensao_data = %d: ", 
					keyUserId, 
					keyDimensaoData);
			System.out.println(msg);
			GroundtruthRanking keyRanking = ranking.get(keyUserId);
			RankingPosition maxRanking = keyRanking.maxRanking();
			int rankingSize = maxRanking.getStartPosition() + maxRanking.getTrajs().size();
			List<String> rankedPreviously = keyRanking.getRankedTrajectories().stream().map(t -> t.getUserId() + "/" + t.getDimensaoData()).collect(Collectors.toList());
			List<String> rankingByMeasure = entries.stream().map(t -> (String) t.getTrajectoryId()).collect(Collectors.toList());
			List<Boolean> isPreviouslyRanked = rankingByMeasure.stream().map(traj -> rankedPreviously.contains(traj)).collect(Collectors.toList());
			
			double[] naturalOrder = IntStream.range(0, rankedPreviously.size()).mapToDouble(i -> (double) i).toArray();
			double[] measureOrder = IntStream.range(0, rankedPreviously.size()).mapToDouble(i -> (double) rankingByMeasure.indexOf(rankedPreviously.get(i))).toArray();
			double spearmanCorrelation = new SpearmansCorrelation().correlation(naturalOrder, measureOrder);

			double bprefs = bprefs(isPreviouslyRanked.toArray(new Boolean[isPreviouslyRanked.size()]), rankedPreviously.size(), rankingByMeasure.size() - rankedPreviously.size());

			double ndcg = NDCG.compute(rankingByMeasure, rankedPreviously, null);
			stats.computeIfAbsent("Bprefs", (t) -> new DescriptiveStatistics()).addValue(bprefs);
			stats.computeIfAbsent("NDCG", (t) -> new DescriptiveStatistics()).addValue(ndcg);
			stats.computeIfAbsent("Spearman", (t) -> new DescriptiveStatistics()).addValue(spearmanCorrelation);
			System.out.printf("\tcolab = %d, dimensao_data = %d, NDCG = %.4f, bprefs = %.4f, Spearman = %.4f\n",
					keyUserId, 
					keyDimensaoData,
					ndcg,
					bprefs,
					spearmanCorrelation);
			for (int i = 0; i < entries.size(); i++) {
				SemanticTrajectory t = entries.get(i);
				Integer tUserId = InvolvesDatabaseReader.USER_ID.getData(t, 0);
				Integer tDimensaoData = InvolvesDatabaseReader.DIMENSAO_DATA.getData(t, 0);
				System.out.printf("\tcolab = %d, dimensao_data = %d, distancia = %.4f\n",
						tUserId, 
						tDimensaoData,
						1 - similarityCalculator.getSimilarity(key, t));
				
				if(keyRanking.hasTrajectorynRanking(tUserId, tDimensaoData)) {
					RankingPosition trajectorynRanking = keyRanking.getTrajectorynRanking(tUserId, tDimensaoData);
					if(!trajectorynRanking.isInRankingRanging(i + 1)) {
						errorsCount++;
						
						double startPosition = trajectorynRanking.getStartPosition().doubleValue();
						Number endPosition = trajectorynRanking.getEndPosition();
						endPosition = (endPosition == null ? startPosition + trajectorynRanking.getTrajs().size() : endPosition);
						meanRankingError += Math.abs((startPosition + (endPosition.doubleValue() - startPosition) / 2) - (i + 1));
						
						System.out.printf("\tcolab = %d, dimensao_data = %d, distancia = %.4f - Out of ranking\n",
								tUserId, 
								tDimensaoData,
								1 - similarityCalculator.getSimilarity(key, t));
					}
				} else {
					if(rankingSize > i + 1) {
						errorsCount++;
						
						meanRankingError += Math.abs((rankingSize + Math.abs(rankingSize - entries.size()) / 2) - (i + 1));
						
						System.out.printf("\tcolab = %d, dimensao_data = %d, distancia = %.4f - Beyond ranking\n",
								tUserId, 
								tDimensaoData,
								1 - similarityCalculator.getSimilarity(key, t));
					}
				}
			}
		}
		for (Map.Entry<String, DescriptiveStatistics> entry : stats.entrySet()) {
			System.out.printf("'%s' - %s\n", entry.getKey(), entry.getValue().toString());
		}
		System.out.printf("Mislabeled trajectories: %d\n", errorsCount);
		System.out.printf("Mean ranking error: %.4f\n", meanRankingError);
	}
	
	private static double bprefs(Boolean[] elements, int total_relevant, int total_non_relevant) {
		double nonRelevantCounts = 0;
		double ret = 0;
		for (int i = 0; i < elements.length; i++) {
			if(elements[i]) {
	            ret += (1.0 - (1.0 * Math.min(nonRelevantCounts,total_relevant) / Math.min(total_relevant,total_non_relevant)));
			} else {
				nonRelevantCounts++;
			}
		}
		if(total_relevant > 0) {
			ret /= total_relevant;
		}
		return ret;
	}

	private static List<SemanticTrajectory> findMostSimilar(SemanticTrajectory traj, SemanticTrajectory[] allData, TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator) {
		Map<SemanticTrajectory, Double> ret = new HashMap<>();
		for (SemanticTrajectory trajectory : allData) {
			double similarity = similarityCalculator.getSimilarity(traj, trajectory);
			ret.put(trajectory, 1 - similarity);
		}
		return ret.entrySet()//
				.stream()//
				.sorted(Comparator.comparing(Map.Entry::getValue))//
				.map((entry) -> entry.getKey())//
				.collect(Collectors.toList());
	}
}
