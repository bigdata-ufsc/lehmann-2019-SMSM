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
		return p.equals(d) ? 0 : Double.MAX_VALUE;
	}

	@Override
	public double convert(double units) {
		return units;
	}

}
