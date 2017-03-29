package br.ufsc.lehmann.method;

import java.util.Comparator;

import br.ufsc.lehmann.method.MSTP.ComplexSemanticType;

public class ComplexSemanticTypeComparator implements Comparator<ComplexSemanticType> {

	@Override
	public int compare(ComplexSemanticType o1, ComplexSemanticType o2) {
		for (int i = 0; i < o1.data.length; i++) {
			int compareTo = ((Comparable) o1.data[i]).compareTo(o2.data[i]);
			if(compareTo != 0) {
				return compareTo;
			}
		}
		return 0;
	}

}
