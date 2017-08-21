package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import smile.math.Random;

public enum EnumProblem {
//	NEWYORK_BUS(new NewYorkBusProblem(), 258),// 
	NEWYORK_BUS_2_LINES_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, "MTA NYCT_Q20A", "MTA NYCT_M102"), 2),// 
//	DUBLIN_BUS(new DublinBusProblem(), 465),// 
//	DUBLIN_BUS_2_LINES(new DublinBusProblem("017A0002", "00791001"), 2),//
//	DUBLIN_BUS_2_LINES_STREET_NAME(new DublinBusProblem(DublinBusDataReader.STOP_STREET_NAME_SEMANTIC, "017A0002", "00791001"), 2),//
//	TDRIVE(new TDriveProblem()),//
//	NEWYORK_BIKE(new NYBikeProblem()), //
//	SERGIPE_TRACKS(new SergipeTracksProblem(), 2),//
//	PISA(new PisaProblem(), 2),//
//	PATEL_ANIMALS(new PatelProblem("animal"), 3),//
//	PATEL_HURRICANE(new PatelProblem("hurricane"), 5),//
//	PATEL_HURRICANE_1vs4(new PatelProblem("hurricane_1vs4", "hurricane"), 2),//
//	PATEL_HURRICANE_2vs3(new PatelProblem("hurricane_2vs3", "hurricane"), 2),//
//	PATEL_HURRICANE_tsvs45(new PatelProblem("hurricane_tsvs45", "hurricane"), 2),//
//	PATEL_HURRICANE_12vs45(new PatelProblem("hurricane_12vs45", "hurricane"), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL(new SanFranciscoCab_AirportMallRoad_Problem(new Integer[] {101, 280}, true, true), 2),//
//	TAXI_SANFRANCISCO(new SanFranciscoCabProblem(), 2),//
//	PATEL_VEHICLE_URBAN(new PatelProblem("vehicle_urban", "vehicle"), 2),//
//	SYNTHETIC(new NElementProblem(50, 5), 5)
	;
	private Problem p;
	private int numClasses;

	private EnumProblem(Problem p, int numClasses) {
		this.p = p;
		this.numClasses = numClasses;
	}

	public Problem problem(Random r) {
		p.initialize(r);
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
