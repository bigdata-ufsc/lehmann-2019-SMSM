package br.ufsc.core.trajectory;

public interface IDistanceFunction<T> {

	double distance(T p, T d);
	
	double convert(double units);

}
