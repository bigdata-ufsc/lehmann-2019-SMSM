package br.ufsc.lehmann;

import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_Regions_Problem;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;
import smile.math.Random;

public enum EnumProblem {
	/**
	 * Trajectories constructed with only Stops&Moves
	 */
//	AMSTERDAM_2005(new AmsterdamPark2005Problem(), 6),
//	GEOLIFE_WITH_POIS_UNIVERSITY(new GeolifeUniversitySubProblem(GeolifeUniversityDatabaseReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, true), 4),
	TAXI_SANFRANCISCO_REGIONS_DIRECTIONS_IN_ROADS_DEFINED_REGIONS(new SanFranciscoCab_Regions_Problem(SanFranciscoCabDataReader.STOP_REGION_SEMANTIC, StopMoveStrategy.SMoT, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"}, new String[] {"mall", "intersection_101_280", "bayshore_fwy", "airport"}, true), 12),//
//	INVOLVES(new InvolvesProblem(InvolvesDatabaseReader.STOP_CENTROID_SEMANTIC, StopMoveStrategy.SMoT, true), 89),
//	INVOLVES_RAW_POINTS(new InvolvesProblem(InvolvesDatabaseReader.STOP_NAME_SEMANTIC, false, false, "_com_auditoria", "_com_auditoria_200mts_30_mins"), 7),
//	INVOLVES(new InvolvesProblem(InvolvesDatabaseReader.STOP_NAME_SEMANTIC, true, false, "_com_auditoria", "_com_auditoria_200mts_30_mins"), 7),
//	INVOLVES_CHECKIN_MANUAL(new InvolvesProblem(InvolvesDatabaseReader.STOP_NAME_SEMANTIC, true, false, "_com_auditoria", "_com_auditoria_checkin_manual"), 7)
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
