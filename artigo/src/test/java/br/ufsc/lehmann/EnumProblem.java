package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_Regions_Problem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;
import smile.math.Random;

public enum EnumProblem {
	/**
	 * Trajectories constructed with all coordinates points
	 */
//	NEWYORK_BUS(new NewYorkBusProblem(), 258),// 
//	NEWYORK_BUS_2_LINES_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, "MTA NYCT_Q20A", "MTA NYCT_M102"), 2),//
//	DUBLIN_BUS(new DublinBusProblem(), 465),// 
//	DUBLIN_BUS_2_LINES(new DublinBusProblem("00671001", "00431001"), 2),//
//	DUBLIN_BUS_2_LINES_STREET_NAME(new DublinBusProblem(DublinBusDataReader.STOP_STREET_NAME_SEMANTIC, "00671001", "00431001"), 2),//
//	TDRIVE(new TDriveProblem()),//
//	NEWYORK_BIKE(new NYBikeProblem()), //
//	SERGIPE_TRACKS(new SergipeTracksProblem(), 2),//
//	PISA(new PisaProblem(), 2),//
//	PISA_STREET_NAME(new PisaProblem(PisaDataReader.STOP_STREET_NAME_SEMANTIC, false), 7),//
//	PATEL_ANIMALS(new PatelProblem("animal"), 3),//
//	PATEL_HURRICANE(new PatelProblem("hurricane"), 5),//
//	PATEL_HURRICANE_1vs4(new PatelProblem("hurricane_1vs4", "hurricane"), 2),//
//	PATEL_HURRICANE_2vs3(new PatelProblem("hurricane_2vs3", "hurricane"), 2),//
//	PATEL_HURRICANE_tsvs45(new PatelProblem("hurricane_tsvs45", "hurricane"), 2),//
//	PATEL_HURRICANE_12vs45(new PatelProblem("hurricane_12vs45", "hurricane"), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL(new SanFranciscoCab_AirportMallRoad_Problem(new Integer[] {101, 280}, true, true), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_STREET_NAME(new SanFranciscoCab_AirportMallRoad_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, new Integer[] {101, 280}, true, true), 2),//
//	TAXI_SANFRANCISCO(new SanFranciscoCabProblem(), 2),//
//	PATEL_VEHICLE_URBAN(new PatelProblem("vehicle_urban", "vehicle"), 2),//
//	PATEL_VEHICLE_URBAN_STREET_NAME(new PatelProblem(PatelDataReader.STOP_STREET_NAME_SEMANTIC, "vehicle_urban", "vehicle"), 2),//
//	SYNTHETIC(new NElementProblem(50, 5), 5),
	/**
	 * Trajectories constructed with only Stops&Moves
	 */
//	NEWYORK_BUS_2_LINES_ONLY_STOPS_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, true, "MTA NYCT_Q20A", "MTA NYCT_M102"), 2),//
//	NEWYORK_BUS_2_LINES_BM2_BM3_ONLY_STOPS_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, true, "MTABC_BM3", "MTABC_BM2"), 2),//
//	NEWYORK_BUS_2_LINES_Q52_Q53_ONLY_STOPS_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, true, "MTABC_Q52", "MTABC_Q53"), 2),//
//	NEWYORK_BUS_2_LINES_S79_S59_X1_ONLY_STOPS_STREET_NAME(new NewYorkBusProblem(NewYorkBusDataReader.STOP_STREET_NAME_SEMANTIC, true, "MTA NYCT_S79+", "MTA NYCT_S59", "MTA NYCT_X1"), 3),// 
//	DUBLIN_BUS_2_LINES_ONLY_STOPS_STREET_NAME(new DublinBusProblem(DublinBusDataReader.STOP_STREET_NAME_SEMANTIC, true, "00671001", "00431001"), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_ONLY_STOPS_STREET_NAME(new SanFranciscoCab_AirportMallRoad_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, true, new String[] {"101", "280"}), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_JUST_DIRECTION_ONLY_STOPS_STREET_NAME(new SanFranciscoCab_AirportMallDirection_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, true, new String[] {}, new String[] {"mall to airport", "airport to mall"}), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_DIRECTION_THROUGH_ROAD_ONLY_STOPS_STREET_NAME(new SanFranciscoCab_AirportMallDirection_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, true, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}), 4),//
	TAXI_SANFRANCISCO_REGIONS_DIRECTIONS_IN_ROADS_ONLY_STOPS_REGION(new SanFranciscoCab_Regions_Problem(SanFranciscoCabDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}, new String[] {}, true), 4),//
//	TAXI_SANFRANCISCO_REGIONS_DIRECTIONS_ONLY_STOPS_STREET_NAME(new SanFranciscoCab_Regions_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, null, new String[] {"mall to airport", "airport to mall"}, new String[] {"mall", "airport"}, true), 4),//
//	TAXI_SANFRANCISCO_REGIONS_ONLY_STOPS_STREET_NAME(new SanFranciscoCab_Regions_Problem(SanFranciscoCabDataReader.STOP_STREET_NAME_SEMANTIC, null, new String[] {}, new String[] {"mall", "airport"}, true), 2),//
//	VEHICLE_URBAN_ONLY_STOPS_STREET_NAME(new VehicleProblem(VehicleDataReader.STOP_STREET_NAME_SEMANTIC, true), 2),//
//	PISA_ONLY_STOPS_PLACES_ALL_USERS(new PisaProblem(PisaDataReader.STOP_NAME_SEMANTIC, StopMoveStrategy.SMoT, true, 1, 2, 3, 4, 5, 6, 7), 7),//
//	PISA_ONLY_STOPS_PLACES_ALL_USERS(new PisaProblem(PisaDataReader.STOP_NAME_SEMANTIC, StopMoveStrategy.SMoT, true, 1, 2, 3), 3),//
//	PISA_ONLY_STOPS_STREET_NAME_ALL_USERS(new PisaProblem(PisaDataReader.STOP_STREET_NAME_SEMANTIC, true, 1, 2, 3, 4, 5, 6, 7), 7),//
//	PISA_ONLY_STOPS_STREET_NAME_USERS_4_6(new PisaProblem(PisaDataReader.STOP_STREET_NAME_SEMANTIC, true, 4, 6), 2),//
//	PISA_ONLY_STOPS_STREET_NAME_USERS_2_3(new PisaProblem(PisaDataReader.STOP_STREET_NAME_SEMANTIC, true, 2, 3), 2),//
//	PROTOTYPE(new PrototypeProblem(), 2),
//	NEWYORK_BUS_2_LINES_ONLY_STOPS_TRAFFIC_LIGHT(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, "MTA NYCT_Q20A", "MTA NYCT_M102"), 2),//
//	NEWYORK_BUS_2_LINES_BM2_BM3_ONLY_STOPS_TRAFFIC_LIGHT(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, "MTABC_BM3", "MTABC_BM2"), 2),//
//	NEWYORK_BUS_2_LINES_Q52_Q53_ONLY_STOPS_TRAFFIC_LIGHT(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, "MTABC_Q52", "MTABC_Q53"), 2),//
//	NEWYORK_BUS_2_LINES_S79_S59_X1_ONLY_STOPS_TRAFFIC_LIGHT(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, "MTA NYCT_S79+", "MTA NYCT_S59", "MTA NYCT_X1"), 3),//
//	NEWYORK_BUS_2_LINES_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, "MTA NYCT_Q20A", "MTA NYCT_M102"), 2),//
//	NEWYORK_BUS_2_LINES_BM2_BM3_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, "MTABC_BM3", "MTABC_BM2"), 2),//
//	NEWYORK_BUS_2_LINES_Q52_Q53_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, "MTABC_Q52", "MTABC_Q53"), 2),//
//	NEWYORK_BUS_2_LINES_S79_S59_X1_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new NewYorkBusProblem(NewYorkBusDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, "MTA NYCT_S79+", "MTA NYCT_S59", "MTA NYCT_X1"), 3),// 
//	DUBLIN_BUS_2_LINES_ONLY_STOPS_TRAFFIC_LIGHT(new DublinBusProblem(DublinBusDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, "00671001", "00431001"), 2),// 
//	DUBLIN_BUS_2_LINES_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new DublinBusProblem(DublinBusDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, "00671001", "00431001"), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_ONLY_STOPS_TRAFFIC_LIGHT(new SanFranciscoCab_AirportMallRoad_Problem(SanFranciscoCabDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, new String[] {"101", "280"}), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_DIRECTION_ONLY_STOPS_TRAFFIC_LIGHT(new SanFranciscoCab_AirportMallDirection_Problem(SanFranciscoCabDataReader.STOP_TRAFFIC_LIGHT_SEMANTIC, true, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new SanFranciscoCab_AirportMallRoad_Problem(SanFranciscoCabDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, new String[] {"101", "280"}), 2),//
//	TAXI_SANFRANCISCO_AIRPORT_MALL_DIRECTION_ONLY_STOPS_TRAFFIC_LIGHT_DISTANCE(new SanFranciscoCab_AirportMallDirection_Problem(SanFranciscoCabDataReader.STOP_TRAFFIC_LIGHT_DISTANCE_SEMANTIC, true, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}), 2),//
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
