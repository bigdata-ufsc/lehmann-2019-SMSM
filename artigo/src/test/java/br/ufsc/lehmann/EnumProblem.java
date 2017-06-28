package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public enum EnumProblem {
	NEWYORK_BUS(new NewYorkBusProblem()),// 
//	DUBLIN_BUS(new DublinBusProblem()),//
//	NEWYORK_BIKE(new NYBikeProblem()), //
	PATEL_ANIMALS(new PatelProblem("animal")),//
	PATEL_HURRICANE(new PatelProblem("hurricane")),//
	PATEL_VEHICLE_URBAN(new PatelProblem("vehicle_urban", "vehicle")),//
	SYNTHETIC(new NElementProblem(20, 5));
	
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
