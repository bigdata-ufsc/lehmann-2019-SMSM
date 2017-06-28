package br.ufsc.lehmann.classifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.IClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.KNNTrainer;
import br.ufsc.lehmann.msm.artigo.validation.Accuracy;
import br.ufsc.lehmann.msm.artigo.validation.ClassificationMeasure;
import br.ufsc.lehmann.msm.artigo.validation.FDR;
import br.ufsc.lehmann.msm.artigo.validation.FMeasure;
import br.ufsc.lehmann.msm.artigo.validation.Fallout;
import br.ufsc.lehmann.msm.artigo.validation.Precision;
import br.ufsc.lehmann.msm.artigo.validation.Recall;
import br.ufsc.lehmann.msm.artigo.validation.Specificity;
import br.ufsc.lehmann.msm.artigo.validation.Validation;

@RunWith(Parameterized.class)
public abstract class AbstractClassifierTest {
	
    @Rule public TestName name = new TestName();
    
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
	private Problem problem;
	
	public AbstractClassifierTest(EnumProblem problemDescriptor) {
		problem = problemDescriptor.problem();
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@Test
	public void selfClassification() throws Exception {
		NElementProblem problem = new NElementProblem(1, 1);
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		stats.put("0", new DescriptiveStatistics());
		TestClassificationExecutor executor = new TestClassificationExecutor(stats);

		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		try {
			executor.classifyProblem(problem, classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(1.0, stats.get("0").getMean(), 0.000001);
		assertEquals(1, stats.get("0").getValues().length);
	}

	@Test
	public void simpleClassification() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
		TestClassificationExecutor executor = new TestClassificationExecutor(stats);
		NElementProblem problem = new NElementProblem(15, 5);
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		try {
			executor.classifyProblem(problem, classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 5; i++) {
			assertEquals(1.0, stats.get(String.valueOf(i)).getMean(), 0.000001);
			assertEquals(2, stats.get(String.valueOf(i)).getValues().length);
		}
	}
	
	@Test
	public void validation_accuracy() throws Exception {
//		NElementProblem problem = new NElementProblem(150, 10);
		List<SemanticTrajectory> testingData = problem.testingData();
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < validateData.length; i++) {
			validateLabelData[i] = discriminator.getData(validateData[i], 0);
		}
		SemanticTrajectory[] testData = testingData.toArray(new SemanticTrajectory[testingData.size()]);
		Object[] testLabelData = new Object[testData.length];
		for (int i = 0; i < testLabelData.length; i++) {
			testLabelData[i] = discriminator.getData(testData[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		
		IClassifier<Object> train = new KNNTrainer<>().train(trainData, discriminator, classifier);
		double testingAccuracy = validation.<Object> test(train, testData, testLabelData, new Accuracy());
		System.out.println(testingAccuracy);
		assertTrue("Testing accuracy = " + testingAccuracy, testingAccuracy > .95);
		
		double validationAccuracy = validation.<Object> test(train, validateData, validateLabelData, new Accuracy());
		System.out.println(validationAccuracy);
		assertTrue("Validation Accuracy = " + validationAccuracy, validationAccuracy > .8);
	}
	
	@Test
	public void validation_precision_recall() throws Exception {
//		NElementProblem problem = new NElementProblem(150, 10);
		List<SemanticTrajectory> testingData = problem.testingData();
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < validateData.length; i++) {
			validateLabelData[i] = discriminator.getData(validateData[i], 0);
		}
		SemanticTrajectory[] testData = testingData.toArray(new SemanticTrajectory[testingData.size()]);
		Object[] testLabelData = new Object[testData.length];
		for (int i = 0; i < testLabelData.length; i++) {
			testLabelData[i] = discriminator.getData(testData[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		ClassificationMeasure[] measures = new ClassificationMeasure[] {//
				new Precision(),//
				new Recall(),//
				new FMeasure(),//
				new Specificity(),//
				new Fallout(),//
				new FDR()
				};

		IClassifier<Object> train = new KNNTrainer<>().train(trainData, discriminator, classifier);
		double[] testingAccuracy = validation.<Object> test(train, testData, testLabelData, new Binarizer(testLabelData[0]), measures);
		System.out.println(Arrays.toString(testingAccuracy));
		assertTrue("Testing accuracy = " + testingAccuracy[0], testingAccuracy[0] > .95);
		
		double[] validationAccuracy = validation.<Object> test(train, validateData, validateLabelData, new Binarizer(validateLabelData[0]), measures);
		System.out.println(Arrays.toString(validationAccuracy));
		assertTrue("Validation Accuracy = " + validationAccuracy[0], validationAccuracy[0] > .8);
	}
	
	@Test
	@Ignore
	public void crossValidation_10_accuracy() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 15; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
		NElementProblem problem = new NElementProblem(150, 10);
		SemanticTrajectory[] data = problem.data().toArray(new SemanticTrajectory[problem.data().size()]);
		Object[] dataLabel = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			dataLabel[i] = problem.discriminator().getData(data[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		double accuracy = validation.<SemanticTrajectory> cv(10, new KNNTrainer<>(), data, dataLabel, new Accuracy());
		System.out.println(accuracy);
		assertTrue("Accuracy = " + accuracy, accuracy > .8);
	}
	
	@Test
	@Ignore
	public void crossValidation_10_precision_recall() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 15; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
//		NElementProblem problem = new NElementProblem(150, 10);
		SemanticTrajectory[] data = problem.data().toArray(new SemanticTrajectory[problem.data().size()]);
		Object[] dataLabel = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			dataLabel[i] = problem.discriminator().getData(data[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		ClassificationMeasure[] measures = new ClassificationMeasure[] {//
				new Precision(),//
				new Recall(),//
				new FMeasure(),//
				new Specificity(),//
				new Fallout(),//
				new FDR()
				};
		double[] precision = validation.<SemanticTrajectory> cv(10, new KNNTrainer<>(), data, dataLabel, new Binarizer(dataLabel[0]), measures);
		System.out.println(Arrays.toString(precision));
		assertTrue("Precision = " + precision[0], precision[0] > .8);
		assertTrue("Recall = " + precision[1], precision[1] > .8);
		assertTrue("F-Measure = " + precision[2], precision[2] > .8);
		assertTrue("Specificity = " + precision[3], precision[3] > .8);
		assertTrue("False alarm rate = " + precision[4], precision[4] < .2);
		assertTrue("False discovery rate = " + precision[5], precision[5] < .2);
	}
}
