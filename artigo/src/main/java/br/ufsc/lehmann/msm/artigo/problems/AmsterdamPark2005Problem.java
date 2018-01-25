package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class AmsterdamPark2005Problem extends AbstractProblem {
	
	private boolean onlyStops;
	private Integer[] users;
	private StopMoveStrategy strategy;

	public AmsterdamPark2005Problem(Integer... users) {
		this(false, users);
	}

	public AmsterdamPark2005Problem(boolean onlyStops, Integer... users) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public AmsterdamPark2005Problem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public AmsterdamPark2005Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.users = users;
	}

	@Override
	public Semantic discriminator() {
		return AmsterdamPark2005DatabaseReader.TEAM;
	}

	@Override
	public String shortDescripton() {
		return "Amsterdan 2005 [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new AmsterdamPark2005DatabaseReader(onlyStops).read());
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
