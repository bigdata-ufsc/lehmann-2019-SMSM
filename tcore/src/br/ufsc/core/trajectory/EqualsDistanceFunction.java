package br.ufsc.core.trajectory;

public class EqualsDistanceFunction<T extends Object> implements IDistanceFunction<T> {

	@Override
	public double distance(T p, T d) {
		if(p == d) {
			return 0;
		}
		if (p == null || d == null) {
			return Double.MAX_VALUE;
		}
		return p.equals(d) ? 0 : Double.MAX_VALUE;
	}

	@Override
	public double convert(double units) {
		return units;
	}

}
