package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class InvolvesProblem extends AbstractProblem {
	
	private boolean onlyStops;
	private Integer[] users;
	private StopMoveStrategy strategy;
	private String year_month;

	public InvolvesProblem(Integer... users) {
		this(false, users);
	}

	public InvolvesProblem(boolean onlyStops, Integer... users) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public InvolvesProblem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public InvolvesProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, StopMoveStrategy.CBSMoT, null, false);
	}
	
	public InvolvesProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, String year_month, boolean onlyStops, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.year_month = year_month;
		this.onlyStops = onlyStops;
		this.users = users;
	}

	public InvolvesProblem(boolean b, String year_month) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, StopMoveStrategy.CBSMoT, year_month, false);
	}

	@Override
	public Semantic discriminator() {
		return InvolvesDatabaseReader.TRAJECTORY_IDENTIFIER;
	}

	@Override
	public String shortDescripton() {
		return "Involves " + (year_month != null ? "(year_month " + year_month + ")" : "") + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new InvolvesDatabaseReader(onlyStops, year_month).read());
		} catch (NumberFormatException | InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException  e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new PisaDatabaseReader(onlyStops).read(users));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
