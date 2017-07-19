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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.classifier.Binarizer;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.algorithms.IClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.algorithms.ITrainer;
import smile.math.Math;

/**
 * A utility class for validating predictive models on test data.
 * 
 * @author Haifeng
 */
public class Validation {
	
	private Problem problem;
	private IMeasureDistance<SemanticTrajectory> measure;

	public Validation(Problem problem, IMeasureDistance<SemanticTrajectory> measure) {
		this.problem = problem;
		this.measure = measure;
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
        
        CrossValidation cv = new CrossValidation(n, k);
        double result = 0.0;
        for (int i = 0; i < k; i++) {
        	final int finalI = i;
            SemanticTrajectory[] trainx = Math.slice(x, cv.train[i]);
            
            IClassifier<Label> classifier = trainer.train(trainx, problem.discriminator(), this.measure);

            Object[] predictions = new Object[cv.test[i].length];
            Object[] real = new Object[cv.test[i].length];
            IntStream.iterate(0, l -> l + 1).limit(cv.test[i].length).parallel().forEach((l) -> {
            	predictions[l] = classifier.classify(x[cv.test[finalI][l]]);
            	real[l] = y[cv.test[finalI][l]];
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

        CrossValidation cv = new CrossValidation(n, k);
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
