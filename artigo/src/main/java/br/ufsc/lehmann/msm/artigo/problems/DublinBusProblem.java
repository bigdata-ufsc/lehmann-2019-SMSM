package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class DublinBusProblem extends AbstractProblem {
	private String[] lines;
	private boolean onlyStops;

	public DublinBusProblem(String... lines) {
		this(DublinBusDataReader.STOP_CENTROID_SEMANTIC, lines);
	}

	public DublinBusProblem(StopSemantic stopSemantic, String... lines) {
		this(stopSemantic, false, lines);
	}

	public DublinBusProblem(StopSemantic stopSemantic, boolean onlyStops, String... lines) {
		super(stopSemantic);
		this.onlyStops = onlyStops;
		this.lines = lines;
	}

	@Override
	public Semantic discriminator() {
		return DublinBusDataReader.LINE_INFO;
	}

	@Override
	public String shortDescripton() {
		return "Dublin bus" + (lines != null ? "(lines=" + lines.length + ")" : "") + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}

	protected List<SemanticTrajectory> load() {
//		try {
//			return new ArrayList<>(new DublinBusDataReader(onlyStops).read(lines));
//		} catch (NumberFormatException | IOException | ParseException e) {
//			throw new RuntimeException(e);
//		}
		try {
			return new ArrayList<>(new DublinBusDatabaseReader(onlyStops).read(lines));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
