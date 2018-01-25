/*******************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufsc.lehmann.msm.artigo.classifiers.validation;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.classifier.Binarizer;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.classifiers.algorithms.IClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.algorithms.ITrainer;
import smile.math.Math;
import smile.math.Random;

/**
 * A utility class for validating predictive models on test data.
 * 
 * @author Haifeng
 */
public class Validation {
	
	private Problem problem;
	private IMeasureDistance<SemanticTrajectory> measure;
	private Random random;

	public Validation(Problem problem, IMeasureDistance<SemanticTrajectory> measure) {
		this(problem, measure, new Random(System.currentTimeMillis()));
	}

	public Validation(Problem problem, IMeasureDistance<SemanticTrajectory> measure, Random random) {
		this.problem = problem;
		this.measure = measure;
		this.random = random;
	}

	public double[] precisionAtRecall(IMeasureDistance<SemanticTrajectory> measureDistance, SemanticTrajectory[] testData, int recallLevel) {
		double[] ret = new double[recallLevel];
		double[][] precisionRecall = new double[testData.length][];
		List<SemanticTrajectory> trajs = Arrays.asList(testData);
		SemanticTrajectory[] trajsArray = trajs.toArray(new SemanticTrajectory[trajs.size()]);
		Table<SemanticTrajectory, SemanticTrajectory, Double> allDistances = ArrayTable.create(trajs, trajs);
		Map<Object, LongAdder> occurrences = new HashMap<>();
		Semantic semantic = problem.discriminator();
		for (int i = 0; i < trajsArray.length; i++) {
			Object classData = semantic.getData(trajsArray[i], 0);
			occurrences.computeIfAbsent(classData, (t) -> new LongAdder()).increment();
		}
		
		ExecutorService executorService = new ThreadPoolExecutor((int) (Runtime.getRuntime().availableProcessors() / 1.5),
				(int) (Runtime.getRuntime().availableProcessors() / 1.5), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		DelayQueue<DelayedDistanceMeasure> queueProcess = new DelayQueue<>();
		for (int i = 0; i < trajsArray.length; i++) {
			int finalI = i;
			for (int j = i; j < trajsArray.length; j++) {
				int finalJ = j;
				Future<Double> future = executorService.submit(new Callable<Double>() {
					
					@Override
					public Double call() throws Exception {
						double distance = measureDistance.distance(trajsArray[finalI], trajsArray[finalJ]);
						return distance;
					}
				});
				queueProcess.add(new DelayedDistanceMeasure(trajsArray[i], trajsArray[j], future, 0));
			}
		}
		while (!queueProcess.isEmpty()) {
			DelayedDistanceMeasure toProcess = queueProcess.poll();
			if (toProcess == null) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			Future<Double> fut = toProcess.distance;
			if (!fut.isDone()) {
				queueProcess.add(new DelayedDistanceMeasure(toProcess.a, toProcess.b, toProcess.distance, 50/* ms */));
			} else {
				try {
					double distance = fut.get();
					allDistances.put(toProcess.a, toProcess.b, distance);
					allDistances.put(toProcess.b, toProcess.a, distance);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		executorService.shutdown();
		for (int i = 0; i < trajsArray.length; i++) {
			List<Map.Entry<SemanticTrajectory, Double>> rows = allDistances.row(trajsArray[i]).entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
			Object classData = semantic.getData(trajsArray[i], 0);
			int groundTruthCounter = occurrences.get(classData).intValue();
			precisionRecall[i] = new double[groundTruthCounter];
			int correctClass = 0;
			for (int j = 0; correctClass < groundTruthCounter && j < testData.length; j++) {
				Entry<SemanticTrajectory, Double> entry = rows.get(j);
				Object otherClassData = semantic.getData(entry.getKey(), 0);
				if(Objects.equals(classData, otherClassData)) {
					precisionRecall[i][correctClass++] = correctClass / (j + 1.0);
				}
			}
		}
		for (int i = 0; i < recallLevel; i++) {
			final int finalI = i;
			ret[i] = Arrays.stream(precisionRecall).mapToDouble(a -> a[Math.min(finalI, (int) ((a.length / (double) recallLevel) * finalI))]).sum() / testData.length;
		}
		return ret;
	}
	
	static class DelayedDistanceMeasure implements Delayed {

		private SemanticTrajectory a;
		private SemanticTrajectory b;
		private Future<Double> distance;
		private long delay;

		DelayedDistanceMeasure(SemanticTrajectory a, SemanticTrajectory b, Future<Double> distance, int delay) {
			this.a = a;
			this.b = b;
			this.distance = distance;
			this.delay = TimeUnit.MILLISECONDS.toNanos(delay);
		}

		@Override
		public int compareTo(Delayed other) {
			if (other == this) // compare zero if same object
				return 0;
			if (other instanceof DelayedDistanceMeasure) {
				DelayedDistanceMeasure x = (DelayedDistanceMeasure) other;
				long diff = delay - x.delay;
				if (diff < 0)
					return -1;
				else if (diff > 0)
					return 1;
				else
					return 1;
			}
			long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
			return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(delay - System.nanoTime(), TimeUnit.NANOSECONDS);
		}

	}

	
    /**
     * Tests a classifier on a validation set.
     * 
     * @param <T> the data type of input objects.
     * @param classifier a trained classifier to be tested.
     * @param x the test data set.
     * @param y the test data labels.
     * @return the accuracy on the test dataset
     */
    public<Label> double test(IClassifier<Label> classifier, SemanticTrajectory[] x, Object[] y) {
        int n = x.length;
        Object[] predictions = new Object[n];
        for (int i = 0; i < n; i++) {
            predictions[i] = classifier.classify(x[i]);
        }
        
        return new Accuracy().measure(y, predictions);
    }
    
    /**
     * Tests a classifier on a validation set.
     * 
     * @param <T> the data type of input objects.
     * @param classifier a trained classifier to be tested.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measure the performance measures of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double test(IClassifier<Label> classifier, SemanticTrajectory[] x, Object[] y, ClassificationMeasure measure) {
        int n = x.length;
        Object[] predictions = new Object[n];
        IntStream.iterate(0, i -> i + 1).limit(n).parallel().forEach((i) -> {
        	predictions[i] = classifier.classify(x[i]);
        });
        
        return measure.measure(y, predictions);
    }
    
    /**
     * Tests a classifier on a validation set.
     * 
     * @param <T> the data type of input objects.
     * @param classifier a trained classifier to be tested.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measures the performance measures of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double[] test(IClassifier<Label> classifier, SemanticTrajectory[] x, Label[] y, Binarizer binarizer, ClassificationMeasure[] measures) {
        int n = x.length;
        Boolean[] groundTruth = new Boolean[y.length];
        for (int i = 0; i < y.length; i++) {
        	groundTruth[i] = binarizer.isTrue(y[i]);
        }
        Object[] predictions = new Object[n];
        IntStream.iterate(0, i -> i + 1).limit(n).parallel().forEach((i) -> {
        	Label classified = classifier.classify(x[i]);
        	predictions[i] = binarizer.isTrue(classified);
        });
        
        int m = measures.length;
        double[] results = new double[m];
        for (int i = 0; i < m; i++) {
            double r = measures[i].measure(groundTruth, predictions);
			results[i] = r;
        }
        
        return results;
    }
    
    /**
     * Leave-one-out cross validation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @return the accuracy on test dataset
     */
    public<Label> double loocv(ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y) {
        int m = 0;
        int n = x.length;
        
        LOOCV loocv = new LOOCV(n);
        for (int i = 0; i < n; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, loocv.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            if (classifier.classify(x[loocv.test[i]]).equals(y[loocv.test[i]])) {
                m++;
            }
        }
        
        return (double) m / n;
    }
    
    /**
     * Leave-one-out cross validation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measure the performance measure of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double loocv(ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, ClassificationMeasure measure) {
        int n = x.length;
        Object[] predictions = new Object[n];
        
        LOOCV loocv = new LOOCV(n);
        for (int i = 0; i < n; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, loocv.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            predictions[loocv.test[i]] = classifier.classify(x[loocv.test[i]]);
        }
        
        return measure.measure(y, predictions);
    }
    
    /**
     * Leave-one-out cross validation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measures the performance measures of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double[] loocv(ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, ClassificationMeasure[] measures) {
        int n = x.length;
        Object[] predictions = new Object[n];
        
        LOOCV loocv = new LOOCV(n);
        for (int i = 0; i < n; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, loocv.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            predictions[loocv.test[i]] = classifier.classify(x[loocv.test[i]]);
        }
        
        int m = measures.length;
        double[] results = new double[m];
        for (int i = 0; i < m; i++) {
            results[i] = measures[i].measure(y, predictions);
        }
        
        return results;
    }
    
    /**
     * Cross validation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param k k-fold cross validation.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measure the performance measure of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double cv(int k, ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, ClassificationMeasure measure) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid k for k-fold cross validation: " + k);
        }
        
        int n = x.length;
		double result = 0.0;
        
        CrossValidation cv = null;
        SemanticTrajectory[][] trains = null;
		for (int t = 0; t < 10; t++) {
			try {
				cv = new CrossValidation(n, k, this.random);
				trains = new SemanticTrajectory[k][];
				for (int i = 0; i < k; i++) {
					SemanticTrajectory[] trainx = Math.slice(x, cv.train[i]);

					List<Label> labels = new ArrayList<>();
					for (SemanticTrajectory traj : trainx) {
						Label data = (Label) problem.discriminator().getData(traj, 0);
						labels.add(data);
					}
					List<Label> uniqueLabels = new ArrayList<>(new LinkedHashSet<>(labels));
					if (uniqueLabels.size() == 1) {
						throw new IllegalStateException("Only one class");
					}
					trains[i] = trainx;
				}
			} catch (IllegalStateException e) {
				System.err.println("Error generating CV");
				cv = null;
			}
		}
		if(cv == null) {
			throw new RuntimeException("Impossibru generate CVs");
		}
		CrossValidation finalCV = cv;
        for (int i = 0; i < k; i++) {
        	final int finalI = i;
            SemanticTrajectory[] trainx = trains[i];
        	
        	IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);
        	
        	Object[] predictions = new Object[cv.test[i].length];
        	Object[] real = new Object[cv.test[i].length];
        	IntStream.iterate(0, l -> l + 1).limit(cv.test[i].length).parallel().forEach((l) -> {
        		predictions[l] = classifier.classify(x[finalCV.test[finalI][l]]);
        		real[l] = y[finalCV.test[finalI][l]];
        	});
        	result += measure.measure(real, predictions);
        }
        
        return result / k;
    }
    
    /**
     * Cross validation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param k k-fold cross validation.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param binarizer 
     * @param measures the performance measures of classification.
     * @return the test results with the same size of order of measures
     */
    public<Label> double[] cv(int k, ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, Binarizer binarizer, ClassificationMeasure[] measures) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid k for k-fold cross validation: " + k);
        }
        
        int n = x.length;
        int m = measures.length;
        double[] results = new double[m];
        Boolean[] groundTruth = new Boolean[y.length];
        for (int i = 0; i < y.length; i++) {
			groundTruth[i] = binarizer.isTrue(y[i]);
		}

        CrossValidation cv = new CrossValidation(n, k, this.random);
        for (int i = 0; i < k; i++) {
        	final int finalI = i;
            SemanticTrajectory[] trainx = Math.slice(x, cv.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            Object[] predictions = new Object[cv.test[i].length];
            Boolean[] real = new Boolean[cv.test[i].length];
            IntStream.iterate(0, j -> j + 1).limit(cv.test[i].length).parallel().forEach((j) -> {
            	Label classifiedAs = classifier.classify(x[cv.test[finalI][j]]);
				predictions[j] = binarizer.isTrue(classifiedAs);
            	real[j] = groundTruth[cv.test[finalI][j]];
            });
            for (int j = 0; j < m; j++) {
            	results[j] += measures[j].measure(real, predictions);
            }
        }
        for (int i = 0; i < results.length; i++) {
        	results[i] /= k;
		}
        
        return results;
    }
    
    /**
     * Bootstrap accuracy estimation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param k k-round bootstrap estimation.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @return the k-round accuracies
     */
    public<Label> double[] bootstrap(int k, ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid k for k-fold bootstrap: " + k);
        }
        
        int n = x.length;
        double[] results = new double[k];
        Accuracy measure = new Accuracy();
        
        Bootstrap bootstrap = new Bootstrap(n, k);
        for (int i = 0; i < k; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, bootstrap.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            int nt = bootstrap.test[i].length;
            Object[] truth = new Object[nt];
            Object[] predictions = new Object[nt];
            for (int j = 0; j < nt; j++) {
                int l = bootstrap.test[i][j];
                truth[j] = y[l];
                predictions[j] = classifier.classify(x[l]);
            }

            results[i] = measure.measure(truth, predictions);
        }
        
        return results;
    }
    
    /**
     * Bootstrap performance estimation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param k k-fold bootstrap estimation.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measure the performance measures of classification.
     * @return k-by-m test result matrix, where k is the number of
     * bootstrap samples and m is the number of performance measures.
     */
    public <Label> double[] bootstrap(int k, ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, ClassificationMeasure measure) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid k for k-fold bootstrap: " + k);
        }
        
        int n = x.length;
        double[] results = new double[k];
        
        Bootstrap bootstrap = new Bootstrap(n, k);
        for (int i = 0; i < k; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, bootstrap.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            int nt = bootstrap.test[i].length;
            Object[] truth = new Object[nt];
            Object[] predictions = new Object[nt];
            for (int j = 0; j < nt; j++) {
                int l = bootstrap.test[i][j];
                truth[j] = y[l];
                predictions[j] = classifier.classify(x[l]);
            }

            results[i] = measure.measure(truth, predictions);
        }
        
        return results;
    }
    
    /**
     * Bootstrap performance estimation of a classification model.
     * 
     * @param <T> the data type of input objects.
     * @param k k-fold bootstrap estimation.
     * @param trainer a classifier trainer that is properly parameterized.
     * @param x the test data set.
     * @param y the test data labels.
     * @param measures the performance measures of classification.
     * @return k-by-m test result matrix, where k is the number of
     * bootstrap samples and m is the number of performance measures.
     */
    public <Label> double[][] bootstrap(int k, ITrainer<Label> trainer, SemanticTrajectory[] x, Object[] y, ClassificationMeasure[] measures) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid k for k-fold bootstrap: " + k);
        }
        
        int n = x.length;
        int m = measures.length;
        double[][] results = new double[k][m];
        
        Bootstrap bootstrap = new Bootstrap(n, k);
        for (int i = 0; i < k; i++) {
            SemanticTrajectory[] trainx = Math.slice(x, bootstrap.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            int nt = bootstrap.test[i].length;
            Object[] truth = new Object[nt];
            Object[] predictions = new Object[nt];
            for (int j = 0; j < nt; j++) {
                int l = bootstrap.test[i][j];
                truth[j] = y[l];
                predictions[j] = classifier.classify(x[l]);
            }

            for (int j = 0; j < m; j++) {
                results[i][j] = measures[j].measure(truth, predictions);
            }
        }
        
        return results;
    }
}
