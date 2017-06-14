package br.ufsc.lehmann.msm.artigo;

public interface IMeasureDistance<T> {

	double distance(T t1, T t2);

	String name();
}
