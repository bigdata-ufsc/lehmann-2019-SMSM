package br.ufsc.lehmann.msm.artigo.problems;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;

public interface IDataReader {
	List<SemanticTrajectory> read();
}
