package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class SergipeTracksProblem extends AbstractProblem {
	
	public SergipeTracksProblem(StopSemantic stopSemantic) {
		super(stopSemantic);
	}

	@Override
	public Semantic discriminator() {
		return SergipeTracksDataReader.CAR_OR_BUS;
	}

	@Override
	public String shortDescripton() {
		return "Sergipe tracks";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new SergipeTracksDataReader().read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
