package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class SanFranciscoCabProblem extends AbstractProblem {
	
	protected boolean onlyStop;

	public SanFranciscoCabProblem(StopSemantic stopSemantic, boolean onlyStop) {
		super(stopSemantic);
		this.onlyStop = onlyStop;
	}
	
	@Override
	public Semantic discriminator() {
		return SanFranciscoCabDataReader.OCUPATION;
	}

	public boolean isRawTrajectory() {
		return !onlyStop;
	}
	
	@Override
	public String shortDescripton() {
		return "San Francisco cab";
	}
	
	protected List<SemanticTrajectory> load() {
//		try {
//			return new ArrayList<>(new SanFranciscoCabDataReader(onlyStop).read());
//		} catch (IOException | ParseException e) {
//			throw new RuntimeException(e);
//		}
		try {
			return new ArrayList<>(new SanFranciscoCabDatabaseReader(onlyStop).read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
