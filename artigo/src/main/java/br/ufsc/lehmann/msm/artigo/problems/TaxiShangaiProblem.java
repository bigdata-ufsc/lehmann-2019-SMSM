package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class TaxiShangaiProblem extends AbstractProblem {
	
	public TaxiShangaiProblem(StopSemantic stopSemantic) {
		super(stopSemantic);
	}

	@Override
	public Semantic discriminator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String shortDescripton() {
		return "Shanghai taxi";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new TaxiShangaiDataReader().read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
