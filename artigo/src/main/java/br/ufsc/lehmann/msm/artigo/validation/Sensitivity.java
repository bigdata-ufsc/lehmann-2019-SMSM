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

package br.ufsc.lehmann.msm.artigo.validation;

/**
 * Sensitivity or true positive rate (TPR) (also called hit rate, recall) is a
 * statistical measures of the performance of a binary classification test.
 * Sensitivity is the proportion of actual positives which are correctly
 * identified as such.
 * <p>
 * TPR = TP / P = TP / (TP + FN)
 * <p>
 * Sensitivity and specificity are closely related to the concepts of type
 * I and type II errors. For any test, there is usually a trade-off between
 * the measures. This trade-off can be represented graphically using an ROC curve.
 * <p>
 * In this implementation, the class label 1 is regarded as positive and all others
 * are regarded as negative.
 * 
 * @author Haifeng Li
 */
public class Sensitivity implements ClassificationMeasure {

    @Override
    public double measure(Object[] truth, Object[] prediction) {
        if (truth.length != prediction.length) {
            throw new IllegalArgumentException(String.format("The vector sizes don't match: %d != %d.", truth.length, prediction.length));
        }

        int tp = 0;
        int p = 0;
        for (int i = 0; i < truth.length; i++) {
            if (truth[i] == Boolean.TRUE) {
                p++;

                if (prediction[i] == Boolean.TRUE) {
                    tp++;
                }
            }
        }

        return (double) tp / p;
    }

    @Override
    public String toString() {
        return "Sensitivity";
    }
}
