package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class GeolifeProblem extends AbstractProblem {
	
	private boolean onlyStops;
	private Integer[] zones;
	private StopMoveStrategy strategy;

	public GeolifeProblem(Integer... users) {
		this(false, users);
	}

	public GeolifeProblem(boolean onlyStops, Integer... users) {
		this(GeolifeDatabaseReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public GeolifeProblem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public GeolifeProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.zones = users;
	}

	@Override
	public Semantic discriminator() {
		return GeolifeDatabaseReader.USER_ID;
	}

	@Override
	public String shortDescripton() {
		return "Geolife [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (zones != null ? "(Users " + Arrays.toString(zones) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new GeolifeDataReader(onlyStops, strategy).read(zones));
		} catch (NumberFormatException | ParseException | IOException e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new GeolifeDatabaseReader(onlyStops).read(zones));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
