package br.ufsc.ftsm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Ordering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;

public class TopK {

//	private static DataSource source;
//	private static DataRetriever retriever;

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
//		source = new DataSource("postgres", "postgis", "localhost", 5432, "postgis", DataSourceType.PGSQL, "crawdad",
//				null, "geom");
//		retriever = source.getRetriever();

//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth1TSToAirportDeparture(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth2TSToUS(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth3USToAirport(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth4TSToAirportArrival(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth5AirportArrivalToUS(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth6TSToP39(),T);
//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth7USToP39(),T);

//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth1TSToAirportDeparture(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth2TSToUS(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth3USToAirport(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth4TSToAirportArrival(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth5AirportArrivalToUS(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth6TSToP39(),T);
//		testMethod("UMSOnlyAlikeness3Pt",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth7USToP39(),T);

//		testMethod("DTW",new DTWDistanceCalculator(), createTaxicabGroundtruth8P39ToTS(),T);
//		testMethod("UMSOA3",new UMSOnlyAlikeness3Pt(), createTaxicabGroundtruth8P39ToTS(),T);
//		testMethod("UMSNSF",new UMSNewSharenessFixed(), createTaxicabGroundtruth8P39ToTS(),T);
//		
//		retriever.prepareFetchTrajectoryStatement();
//	testMethod("DTW",new DTWDistanceCalculator(), createGeolife1to6(),T);
//	testMethod("SDTW",new SDTW(), createGeolife1to6(),T);
		// testMethod("UMS",new UMSMathSimple(), createTaxicabGroundtruth6TSToP39(),T);
//	testMethod("UMS",new UMS(), GT.CD1.getTrajectories(),T);
//	testMethod("UMS",new UMSSpeed(), GT.CD1.getTrajectories(),T);
		
		String stopsTable = "stops_moves.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_stop";
		String movesTable = "stops_moves.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_move";
		String pointsTable = "taxi.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_cleaned";
		List<SemanticTrajectory> T = new SanFranciscoCabDatabaseReader(false, null, null, null, stopsTable, movesTable, pointsTable).read();
		Multimap<String, SemanticTrajectory> Gs = MultimapBuilder.hashKeys().arrayListValues().build();
		BasicSemantic<String> groundtruth = new BasicSemantic<>(SanFranciscoCabDatabaseReader.ROADS_WITH_DIRECTION.getIndex());
		T.stream().forEach(t -> {
			Gs.put(groundtruth.getData(t, 0), t);
		});
		Gs.asMap().entrySet().stream().forEach(e -> {
			if(e.getKey() != null) {
				System.out.println(e.getKey());
				testMethod("UMS", new UMS(), new ArrayList<SemanticTrajectory>(e.getValue()), T);
			}
		});

//		testMethod("UMS Speed",new UMSSpeed(),GT.CD5.getTrajectories(),T);

//		testMethod("UMS",new UMS(), GT.CD6.getTrajectories(),T);
//		testMethod("UMS Speed",new UMSSpeed(),GT.CD6.getTrajectories(),T);
//		
//		testMethod("UMS",new UMS(), GT.CD7.getTrajectories(),T);
//		testMethod("UMS Speed",new UMSSpeed(), GT.CD7.getTrajectories(),T);
//		
//		testMethod("UMS",new UMS(), GT.CD2.getTrajectories(),T);
//		testMethod("UMS Speed",new UMSSpeed(),GT.CD2.getTrajectories(),T);
//		
//		testMethod("UMS",new UMS(), GT.CD3.getTrajectories(),T);
//		testMethod("UMS Speed",new UMSSpeed(),GT.CD3.getTrajectories(),T);
//		
//		testMethod("UMS",new UMS(), GT.CD4.getTrajectories(),T);
//		testMethod("UMS Speed",new UMSSpeed(), GT.CD4.getTrajectories(),T);
//		
//		
//	testMethod("UMS",new UMSSamsung(), GT.CD1.getTrajectories(),T);
//	testMethod("EDwP",new EditDistance(), GT.CD1.getTrajectories(),T);

	}

	private static void testMethod(String name, TrajectorySimilarityCalculator<SemanticTrajectory> tdc, List<SemanticTrajectory> G,
			List<SemanticTrajectory> T) {
		List<List<Double>> precisionAll = new ArrayList<List<Double>>();
		System.out.println("############### Method: " + name + " GT: " + G.size());
		long start = System.currentTimeMillis();
		int k = 0;
		for (SemanticTrajectory t1 : G) {
			// CircleTrajectory e = CreateCircle.createEllipticalTrajectory(t1);
			// ETrajectory e = CreateEllipseJTS.createEllipticalTrajectory(t1);
			List<Score> scoreList = new ArrayList<Score>();
			System.out.print(k + " ");
			k++;
			for (SemanticTrajectory t2 : T) {
				// Trajectory t2 = retriever.fastFetchTrajectory(tid2);
				// ETrajectory e2 =
				// CreateEllipseJTS.createPreparedEllipticalTrajectory(t2);
				double similarity = // ((UMSMSimilarityContinuityV2)tdc).getDistance(e,t2);
						tdc.getSimilarity(t1, t2);// *-1;
				Score score = new Score(similarity, t2);
				scoreList.add(score);
				// System.out.println(similarity);
			}

			precisionAll.add(calculatePrecision(scoreList, G.stream().map(s -> ((Number) s.getTrajectoryId()).intValue()).collect(Collectors.toList())));
		}

		List<Double> avgPrecision = new ArrayList<Double>();
		double sizePrecisionList = precisionAll.get(0).size();

		for (int i = 0; i < sizePrecisionList; i++) {
			double precision = 0.0;
			for (List<Double> precisionList : precisionAll) {
				precision += precisionList.get(i);
			}
			avgPrecision.add(precision / sizePrecisionList);
		}

		for (Double d : avgPrecision) {
			System.out.println(d);
		}

		long end = System.currentTimeMillis();

		System.out.println("Elapsed time (ms): " + (end - start));
	}

	private static List<Double> calculatePrecision(List<Score> scoreList, List<Integer> groundtruth) {
		scoreList = Ordering.natural().greatestOf(scoreList, scoreList.size());
		int groundtruthSize = groundtruth.size();
		ArrayList<Double> result = new ArrayList<Double>();
		double i = 0;
		double j = 0;
		while (j < groundtruthSize && i < scoreList.size()) {

			if (groundtruth.contains(scoreList.get((int) i).getTrajectory().getTrajectoryId())) {
				j++;
				result.add(j / (i + 1));
			}
			i++;
		}
		return result;

	}

}
