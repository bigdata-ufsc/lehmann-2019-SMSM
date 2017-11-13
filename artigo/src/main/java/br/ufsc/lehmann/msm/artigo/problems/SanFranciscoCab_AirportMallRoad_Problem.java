package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public class SanFranciscoCab_AirportMallRoad_Problem extends SanFranciscoCabProblem {
	
	private String[] roads;

	public SanFranciscoCab_AirportMallRoad_Problem(String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, roads);
	}

	public SanFranciscoCab_AirportMallRoad_Problem(StopSemantic stopSemantic, String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, false, roads);
	}
	
	public SanFranciscoCab_AirportMallRoad_Problem(StopSemantic stopSemantic, boolean onlyStop, String[] roads) {
		super(stopSemantic, onlyStop);
		this.roads = roads;
	}
	
	@Override
	public Semantic discriminator() {
		return SanFranciscoCabDataReader.ROAD;
	}

	@Override
	public String shortDescripton() {
		return "San Francisco cab (Airport <-> Mall)[" + stopSemantic().name() + "][onlyStops=" + onlyStop + "]";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new SanFranciscoCabDataReader(onlyStop, roads).read());
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new SanFranciscoCabDatabaseReader(onlyStop, roads).read());
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
