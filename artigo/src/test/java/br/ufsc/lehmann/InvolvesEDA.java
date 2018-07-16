package br.ufsc.lehmann;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.classifier.AbstractClassifierTest;
import br.ufsc.lehmann.classifier.SMSMEllipsesClassifierTest;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import smile.math.Random;

public class InvolvesEDA {

	public static void main(String[] args) {
		Random r = new Random();
		EnumProblem prob = EnumProblem.INVOLVES;
		Problem problem = prob.problem(new Random());
		List<SemanticTrajectory> data = problem.data();
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
//		AbstractClassifierTest t = new UMSClassifierTest(prob);
		AbstractClassifierTest t = new SMSMEllipsesClassifierTest(prob);
		
		TrajectorySimilarityCalculator<SemanticTrajectory> classifier = (TrajectorySimilarityCalculator<SemanticTrajectory>) t.measurer(problem);
		Validation validation = new Validation(problem, (IMeasureDistance<SemanticTrajectory>) classifier, r);

		double[] precisionAtRecall = validation.precisionAtRecall(classifier, allData, 10);
		System.out.printf("Precision@recall(%d): %s\n", 10, ArrayUtils.toString(precisionAtRecall, "0.0"));
		double auc = AUC.precisionAtRecall(precisionAtRecall);
		System.out.printf("AUC: %.4f\n", auc);
	}
}
