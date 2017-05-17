package br.ufsc.lehmann.msm.artigo;

import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public interface IMeasureDistance<T> {

	double distance(DataEntry<T> t1, DataEntry<T> t2);

	String name();
}
