package br.ufsc.core.trajectory;

public class EqualsDistanceFunction implements IDistanceFunction<String> {

	@Override
	public double distance(String p, String d) {
		if(p == d) {
			return 0;
		}
		if (p == null || d == null) {
			return Double.MAX_VALUE;
		}
		return p.compareTo(d);
	}

	@Override
	public double convert(double units) {
		return units;
	}

}
