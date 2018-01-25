package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class AmsterdamPark2006Problem extends AbstractProblem {
	
	private boolean onlyStops;
	private Integer[] users;
	private StopMoveStrategy strategy;

	public AmsterdamPark2006Problem(Integer... users) {
		this(false, users);
	}

	public AmsterdamPark2006Problem(boolean onlyStops, Integer... users) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public AmsterdamPark2006Problem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, users);
	}
	
	public AmsterdamPark2006Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, Integer... users) {
		super(stopSemantic);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.users = users;
	}

	@Override
	public Semantic discriminator() {
		return AmsterdamPark2006DatabaseReader.USER_ID;
	}

	@Override
	public String shortDescripton() {
		return "Amsterdan [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new AmsterdamPark2006DatabaseReader(onlyStops).read());
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
