package br.ufsc.core.trajectory;

public abstract class Semantic<Element, Threshold> {
	public static final Semantic<Number, Number> GID = new Semantic<Number, Number>(0) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j).doubleValue() <= threshlod.doubleValue();
		}

		@Override
		public boolean match(Number d1, Number d2, Number threshlod) {
			return distance(d1, d2) <= threshlod.doubleValue();
		}

		@Override
		public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((Number) a.getDimensionData(index, i), (Number) b.getDimensionData(index, j));
		}
		
		public double distance(Number d1, Number d2) {
			return ((Comparable<Number>) d1).compareTo((Number) d2);
		}
	};
	public static final Semantic<TPoint, Number> SPATIAL_EUCLIDEAN = new EuclideanGeographicalSemantic(1);
	public static final Semantic<TPoint, Number> SPATIAL_LATLON = new LatLonGeographicalSemantic(1);
	public static final Semantic<TemporalDuration, Number> TEMPORAL = new OverlapTemporalSemantic(2);
	
	public static final Semantic<TPoint, Number> SPATIAL = SPATIAL_EUCLIDEAN;
	
	protected int index;
	
	public Semantic(int index) {
		this.index = index;
	}

	public Element getData(SemanticTrajectory p, int i) {
		return (Element) p.getDimensionData(index, i);
	}

	public double similarity(Element d1, Element d2) {
		return 1 - distance(d1, d2);
	}
	
	public double similarity(Element d1, Element d2, Number threshold) {
		if(threshold == null) {
			return similarity(d1, d2);
		}
		double similarity = similarity(d1, d2);
		double t = threshold.doubleValue();
		return similarity >= t ? 1 : similarity;
	}

	public abstract Threshold distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j);

	public abstract boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Threshold threshold);

	public abstract double distance(Element d1, Element d2);

	public abstract boolean match(Element d1, Element d2, Threshold threshold);

	public String description() {
		return String.valueOf(index);
	}
}
