package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public class GeolifeWithTransportationProblem extends GeolifeProblem {

	public GeolifeWithTransportationProblem(String... users) {
		this(false, users);
	}

	public GeolifeWithTransportationProblem(boolean onlyStops, String... users) {
		this(GeolifeDatabaseReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public GeolifeWithTransportationProblem(StopSemantic stopSemantic,boolean onlyStops, String... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public GeolifeWithTransportationProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, String... users) {
		super(stopSemantic, strategy, onlyStops, users);
	}

	@Override
	public Semantic discriminator() {
		return GeolifeDatabaseReader.USER_ID;
	}

	@Override
	public String shortDescripton() {
		return "Geolife [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (zones != null ? "(Users " + Arrays.toString(zones) + ")" : "");
	}

	@Override
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new GeolifeDatabaseReader(onlyStops, true).read(zones));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
