package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public enum EnumProblem {
//	NEWYORK_BUS(new NewYorkBusProblem()),// 
//	DUBLIN_BUS(new DublinBusProblem()),//
//	NEWYORK_BIKE(new NYBikeProblem()), //
	PATEL_ANIMALS(new PatelProblem("animal"), 3),//
//	PATEL_HURRICANE(new PatelProblem("hurricane"), 5),//
	PATEL_HURRICANE_1vs4(new PatelProblem("hurricane_1vs4", "hurricane"), 2),//
	PATEL_HURRICANE_2vs3(new PatelProblem("hurricane_2vs3", "hurricane"), 2),//
	PATEL_HURRICANE_tsvs45(new PatelProblem("hurricane_tsvs45", "hurricane"), 2),//
	PATEL_HURRICANE_12vs45(new PatelProblem("hurricane_12vs45", "hurricane"), 2),//
//	PATEL_VEHICLE_URBAN(new PatelProblem("vehicle_urban", "vehicle"), 3),//
	SYNTHETIC(new NElementProblem(50, 5), 5);
	
	private Problem p;
	private int numClasses;

	private EnumProblem(Problem p, int numClasses) {
		this.p = p;
		this.numClasses = numClasses;
	}

	public Problem problem() {
		return p;
	}
	
	public int numClasses() {
		return numClasses;
	}
	
	@Override
	public String toString() {
		return p.shortDescripton();
	}
}
