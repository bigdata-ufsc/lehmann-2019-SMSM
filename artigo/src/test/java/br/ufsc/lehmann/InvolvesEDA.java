package br.ufsc.lehmann;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.classifier.AbstractClassifierTest;
import br.ufsc.lehmann.classifier.SMSMDTWClassifierTest;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesProblem;
import smile.math.Random;

public class InvolvesEDA {

	public static void main(String[] args) {
		Random r = new Random();
		Problem problem = new InvolvesProblem(true, false, "_com_auditoria", "_com_auditoria_checkin_manual");
		List<SemanticTrajectory> data = problem.data();
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		AbstractClassifierTest t = new SMSMDTWClassifierTest(EnumProblem.INVOLVES);
		
		TrajectorySimilarityCalculator<SemanticTrajectory> classifier = (TrajectorySimilarityCalculator<SemanticTrajectory>) t.measurer(problem);
		Validation validation = new Validation(problem, (IMeasureDistance<SemanticTrajectory>) classifier, r);

		double[] precisionAtRecall = validation.precisionAtRecall(classifier, allData, 10);
		System.out.printf("Precision@recall(%d): %s\n", 10, ArrayUtils.toString(precisionAtRecall, "0.0"));
		double auc = AUC.precisionAtRecall(precisionAtRecall);
		System.out.printf("AUC: %.2f\n", auc);
	}
}
