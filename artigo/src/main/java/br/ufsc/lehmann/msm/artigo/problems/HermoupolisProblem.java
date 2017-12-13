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

public class HermoupolisProblem extends AbstractProblem {
	
	private boolean onlyStops;
	private Integer[] users;
	private StopMoveStrategy strategy;

	public HermoupolisProblem(Integer... users) {
		this(false, users);
	}

	public HermoupolisProblem(boolean onlyStops, Integer... users) {
		this(HermoupolisDataReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public HermoupolisProblem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.SMoT, onlyStops, users);
	}
	
	public HermoupolisProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.users = users;
	}

	@Override
	public Semantic discriminator() {
		return HermoupolisDataReader.USER_ID;
	}

	@Override
	public String shortDescripton() {
		return "Hermoupolis [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new HermoupolisDataReader(onlyStops, strategy).read(users));
		} catch (NumberFormatException | ParseException | IOException e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new PisaDatabaseReader(onlyStops).read(users));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
