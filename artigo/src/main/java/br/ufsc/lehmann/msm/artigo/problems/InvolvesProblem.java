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
	private String stopMove_table;
	private boolean weeklyTrajectories;

	public InvolvesProblem(Integer... users) {
		this(false, users);
	}

	public InvolvesProblem(boolean onlyStops, Integer... users) {
		this(InvolvesDatabaseReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public InvolvesProblem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public InvolvesProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		this(InvolvesDatabaseReader.STOP_CENTROID_SEMANTIC, StopMoveStrategy.CBSMoT, null, null, false, false);
	}
	
	public InvolvesProblem(StopSemantic stopSemantic, StopMoveStrategy strategy, String year_month, String stopMove_table, boolean onlyStops, boolean weeklyTrajectories, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.year_month = year_month;
		this.stopMove_table = stopMove_table;
		this.onlyStops = onlyStops;
		this.weeklyTrajectories = weeklyTrajectories;
		this.users = users;
	}

	public InvolvesProblem(boolean onlyStops, boolean weeklyTrajectories, String year_month, String stopMove_table) {
		this(InvolvesDatabaseReader.STOP_CENTROID_SEMANTIC, StopMoveStrategy.CBSMoT, year_month, stopMove_table, onlyStops, weeklyTrajectories);
	}

	public InvolvesProblem(StopSemantic stopSemantic, boolean onlyStops, boolean weeklyTrajectories, String year_month, String stopMove_table) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, year_month, stopMove_table, onlyStops, weeklyTrajectories);
	}

	@Override
	public Semantic discriminator() {
		return weeklyTrajectories ? InvolvesDatabaseReader.WEEKLY_TRAJECTORY_IDENTIFIER : InvolvesDatabaseReader.TRAJECTORY_IDENTIFIER;
	}

	@Override
	public String shortDescripton() {
		return "Involves " + (year_month != null ? "(year_month " + year_month + ")" : "") + (weeklyTrajectories ? "(weekly)" : "(daily)") + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new InvolvesDatabaseReader(onlyStops, weeklyTrajectories, year_month, stopMove_table).read());
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
