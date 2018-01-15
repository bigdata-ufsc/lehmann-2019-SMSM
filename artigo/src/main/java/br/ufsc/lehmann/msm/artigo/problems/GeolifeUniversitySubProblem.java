package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public class GeolifeUniversitySubProblem extends GeolifeProblem {
	
	private boolean onlyStops;
	private StopMoveStrategy strategy;

	public GeolifeUniversitySubProblem() {
		this(false);
	}

	public GeolifeUniversitySubProblem(boolean onlyStops) {
		this(GeolifeDatabaseReader.STOP_CENTROID_SEMANTIC, onlyStops);
	}

	public GeolifeUniversitySubProblem(StopSemantic stopSemantic,boolean onlyStops) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops);
	}
	
	public GeolifeUniversitySubProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops) {
		super(stopSemantic, strategy, onlyStops);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
	}

	@Override
	public Semantic discriminator() {
		return GeolifeUniversityDataReader.PATH_WITH_DIRECTION;
	}

	@Override
	public String shortDescripton() {
		return "Geolife University [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}

	protected List<SemanticTrajectory> load() {
//		try {
//			return new ArrayList<>(new GeolifeUniversityDataReader(onlyStops, strategy).read());
//		} catch (NumberFormatException | ParseException | IOException e) {
//			throw new RuntimeException(e);
//		}
		try {
			return new ArrayList<>(new GeolifeUniversityDatabaseReader(onlyStops).read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
