package artigo;

import artigo.NearestNeighbour.DataEntry;

public interface IMeasureDistance<T> {

	double distance(DataEntry<T> t1, DataEntry<T> t2);
}
