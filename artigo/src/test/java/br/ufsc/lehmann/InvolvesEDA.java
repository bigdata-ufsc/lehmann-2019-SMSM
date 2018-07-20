package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;
import smile.math.Random;

public class InvolvesEDA {

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
		
		ExecutionPOJO execution = new Gson().fromJson(new FileReader("./src/test/resources/executions/teste.test"), ExecutionPOJO.class);
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		IDataReader dataReader = Datasets.createDataset(dataset);
		List<SemanticTrajectory> data = dataReader.read();
		BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);
		
		Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) similarityCalculator, r);

		double[] precisionAtRecall = validation.precisionAtRecall(similarityCalculator, allData, 10);
		System.out.printf("Precision@recall(%d): %s\n", 10, ArrayUtils.toString(precisionAtRecall, "0.0"));
		double auc = AUC.precisionAtRecall(precisionAtRecall);
		System.out.printf("AUC: %.4f\n", auc);
	}
}
