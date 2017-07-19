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

/**
 * The false discovery rate (FDR) is ratio of false positives
 * to combined true and false positives, which is actually 1 - precision.
 * <p>
 * FDR = FP / (TP + FP)
 *
 * @author Haifeng Li
 */
public class FDR implements ClassificationMeasure {

    @Override
    public double measure(Object[] truth, Object[] prediction) {
        if (truth.length != prediction.length) {
            throw new IllegalArgumentException(String.format("The vector sizes don't match: %d != %d.", truth.length, prediction.length));
        }

        int fp = 0;
        int p = 0;
        for (int i = 0; i < truth.length; i++) {
            if (prediction[i] == Boolean.TRUE) {
                p++;

                if (truth[i] == Boolean.FALSE) {
                    fp++;
                }
            }
        }
        if(p == 0.0) {
        	return 0.0;
        }

        return (double) fp / p;
    }

    @Override
    public String toString() {
        return "False Discovery Rate";
    }
}
