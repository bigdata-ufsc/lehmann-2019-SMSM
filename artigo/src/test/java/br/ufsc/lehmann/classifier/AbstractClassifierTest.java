package br.ufsc.lehmann.classifier;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.IClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.KNNSmileTrainer;
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
	
    private static final FDR FDR = new FDR();
	private static final Fallout FALLOUT = new Fallout();
	private static final Specificity SPECIFICITY = new Specificity();
	private static final FMeasure F_MEASURE = new FMeasure();
	private static final Recall RECALL = new Recall();
	private static final Precision PRECISION = new Precision();
	private static final Accuracy ACCURACY = new Accuracy();

	@Rule public TestName name = new TestName();
    
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }
	private Problem problem;
	private Multimap<ClassificationMeasure, String> measureFailures = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
	
	public AbstractClassifierTest(EnumProblem problemDescriptor) {
		problem = problemDescriptor.problem();
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@After
	public void after() {
		if(!measureFailures.isEmpty()) {
			StringWriter sw = new StringWriter();
			for (Map.Entry<ClassificationMeasure, Collection<String>> entry : measureFailures.asMap().entrySet()) {
				sw.append(entry.getKey().toString()).append(" - [");
				Collection<String> value = entry.getValue();
				for (Iterator iterator = value.iterator(); iterator.hasNext();) {
					String message = (String) iterator.next();
					sw.append(message);
					if(iterator.hasNext()) {
						sw.append(", ");
					}
				}
				sw.append("]\n");
			}
			Assert.fail(sw.toString());
		}
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
			assertEquals(String.format("Class %s - ", String.valueOf(i)), 1.0, stats.get(String.valueOf(i)).getMean(), 0.000001);
			assertEquals(String.format("Class %s - ", String.valueOf(i)), 2, stats.get(String.valueOf(i)).getValues().length);
		}
	}
	
	@Test
	public void validation_accuracy() throws Exception {
		List<SemanticTrajectory> data = problem.data();
		List<SemanticTrajectory> testingData = new ArrayList<>(problem.testingData());
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Object[] allLabelData = new Object[allData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < allData.length; i++) {
			allLabelData[i] = discriminator.getData(allData[i], 0);
		}
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

		KNNSmileTrainer<Object> trainer = new KNNSmileTrainer<>();
		IClassifier<Object> train = trainer.train(trainData, discriminator, classifier);
		double testingAccuracy = validation.<Object> test(train, testData, testLabelData, ACCURACY);
		System.out.println(testingAccuracy);
		assertMeasure(ACCURACY, "Testing = " + testingAccuracy, testingAccuracy > .8);
//		
		double validationAccuracy = validation.<Object> cv(10, trainer, allData, allLabelData, ACCURACY);
		System.out.println(validationAccuracy);
		assertMeasure(ACCURACY, "Validating = " + validationAccuracy, validationAccuracy > .8);
	}
	
	@Test
	public void validation_precision_recall() throws Exception {
		List<SemanticTrajectory> data = problem.data();
		List<SemanticTrajectory> testingData = new ArrayList<>(problem.testingData());
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Object[] allLabelData = new Object[allData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < allData.length; i++) {
			allLabelData[i] = discriminator.getData(allData[i], 0);
		}
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
				PRECISION,//
				RECALL,//
				F_MEASURE,//
				SPECIFICITY,//
				FALLOUT,//
				FDR
				};

		KNNSmileTrainer<Object> trainer = new KNNSmileTrainer<>();
		IClassifier<Object> train = trainer.train(trainData, discriminator, classifier);
		double[] testingMensures = validation.<Object> test(train, testData, testLabelData, new Binarizer(testLabelData[0]), measures);
		System.out.println(Arrays.toString(testingMensures));
		assertMeasure(PRECISION, "Testing = " + testingMensures[0], testingMensures[0] > .8);
		assertMeasure(RECALL, "Testing = " + testingMensures[1], testingMensures[1] > .8);
		assertMeasure(F_MEASURE, "Testing = " + testingMensures[2], testingMensures[2] > .8);
		assertMeasure(SPECIFICITY, "Testing = " + testingMensures[3], testingMensures[3] > .8);
		assertMeasure(FALLOUT, "Testing = " + testingMensures[4], testingMensures[4] < .2);
		assertMeasure(FDR, "Testing = " + testingMensures[5], testingMensures[5] < .2);
		
		double[] validationAccuracy = validation.<Object> cv(10, trainer, allData, allLabelData, new Binarizer(allLabelData[0]), measures);
		System.out.println(Arrays.toString(validationAccuracy));
		assertMeasure(PRECISION, "Validation = " + validationAccuracy[0], validationAccuracy[0] > .8);
		assertMeasure(RECALL, "Validation = " + validationAccuracy[1], validationAccuracy[1] > .8);
		assertMeasure(F_MEASURE, "Validation = " + validationAccuracy[2], validationAccuracy[2] > .8);
		assertMeasure(SPECIFICITY, "Validation = " + validationAccuracy[3], validationAccuracy[3] > .8);
		assertMeasure(FALLOUT, "Validation = " + validationAccuracy[4], validationAccuracy[4] < .2);
		assertMeasure(FDR, "Validation = " + validationAccuracy[5], validationAccuracy[5] < .2);
	}
	
	public void assertMeasure(ClassificationMeasure measure, double expected, double actual, double delta) {
		if(expected != actual) {
			measureFailures.put(measure, "Expected was " + expected + " but actual is " + actual);
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, String message, double expected, double actual, double delta) {
		if(expected != actual) {
			measureFailures.put(measure, message);
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, boolean test) {
		if(!test) {
			measureFailures.put(measure, "Expected true but actual is false");
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, String message, boolean test) {
		if(!test) {
			measureFailures.put(measure, message);
		}
	}
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
}
