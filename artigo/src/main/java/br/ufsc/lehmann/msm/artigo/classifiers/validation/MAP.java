package br.ufsc.lehmann.msm.artigo.classifiers.validation;

import java.util.stream.DoubleStream;

public class MAP {

	public static double precisionAtRecall(double[] args) {
		return DoubleStream.of(args).summaryStatistics().getAverage();
	}
	
}
