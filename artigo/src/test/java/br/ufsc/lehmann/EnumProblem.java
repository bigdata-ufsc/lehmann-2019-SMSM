package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public enum EnumProblem {
//	NEWYORK_BUS(new NewYorkBusProblem()),// 
//	DUBLIN_BUS(new DublinBusProblem()),//
//	NEWYORK_BIKE(new NYBikeProblem()), //
	PATEL_ANIMALS(new PatelProblem("animal")),//
//	PATEL_HURRICANE(new PatelProblem("hurricane")),//
//	PATEL_HURRICANE_1vs4(new PatelProblem("hurricane_1vs4", "hurricane")),//
//	PATEL_HURRICANE_2vs3(new PatelProblem("hurricane_2vs3", "hurricane")),//
//	PATEL_HURRICANE_tsvs45(new PatelProblem("hurricane_tsvs45", "hurricane")),//
//	PATEL_HURRICANE_12vs45(new PatelProblem("hurricane_12vs45", "hurricane")),//
//	PATEL_VEHICLE_URBAN(new PatelProblem("vehicle_urban", "vehicle")),//
	SYNTHETIC(new NElementProblem(50, 5));
	
	private Problem p;

	private EnumProblem(Problem p) {
		this.p = p;
	}

	public Problem problem() {
		return p;
	}
	
	@Override
	public String toString() {
		return p.shortDescripton();
	}
}
