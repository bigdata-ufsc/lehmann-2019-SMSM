package br.ufsc.lehmann.msm.artigo.classifiers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.ComparableStopSemantic;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.MultiThreadClassificationExecutor;
import br.ufsc.lehmann.msm.artigo.classifiers.ERPClassifier.ERPSemanticParameter;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class NewYorkBus_AllClassifiers {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		MultiThreadClassificationExecutor executor = new MultiThreadClassificationExecutor();
		NewYorkBusProblem problem = new NewYorkBusProblem();
		List<IMeasureDistance<SemanticTrajectory>> measures = new ArrayList<>();
//		measures.add(new DTWClassifier(
////				Semantic.GEOGRAPHIC_EUCLIDEAN,//
//				NewYorkBusDataReader.STOP_SEMANTIC));
//		measures.add(new DTWaClassifier(problem, //
//				Semantic.GEOGRAPHIC,//
//				NewYorkBusDataReader.STOP_SEMANTIC));
//		measures.add(new EDRClassifier(//
//				new EDRSemanticParameter(Semantic.GEOGRAPHIC_LATLON, 100.0), //
////				new EDRSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L), //
//				new EDRSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100)
//			));
//		measures.add(new LCSSClassifier(
//				new LCSSSemanticParameter(Semantic.GEOGRAPHIC_LATLON, 100.0), //
////				new LCSSSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L), //
//				new LCSSSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100) //
//			));
//		measures.add(new LiuSchneiderClassifier(
//				new LiuSchneiderParameters(0.5, NewYorkBusDataReader.STOP_SEMANTIC, 100)
//			));
//		measures.add(new MSMClassifier(
//				new MSMSemanticParameter(Semantic.GEOGRAPHIC_LATLON, 100.0, 0.5), //
////				new MSMSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L, 1.0 / 3), //
//				new MSMSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100, 0.5) //
//			));
		measures.add(new MSTPClassifier(//
				Semantic.GEOGRAPHIC, //
//				Semantic.TEMPORAL, //
				new ComparableStopSemantic(NewYorkBusDataReader.STOP_SEMANTIC) //
				));
		measures.add(new MTMClassifier(problem));
		measures.add(new ERPClassifier(new ERPSemanticParameter(//
				NewYorkBusDataReader.STOP_SEMANTIC,//
				new LandmarkStop(new TPoint(0, 0)))));
		for (IMeasureDistance<SemanticTrajectory> measureDistance : measures) {
			try {
				executor.classifyProblem(problem, measureDistance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class LandmarkStop extends Stop {

		public LandmarkStop(TPoint centroid) {
			super(-1, null, null, null, null, null, centroid);
		}
		
	}
}
