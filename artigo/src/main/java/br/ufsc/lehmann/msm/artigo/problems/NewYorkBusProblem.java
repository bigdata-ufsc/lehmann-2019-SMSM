package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;
import cc.mallet.util.ArrayUtils;

public class NewYorkBusProblem extends AbstractProblem {
	
	private String[] lines;
	private boolean onlyStops;
	
	public NewYorkBusProblem(String... lines) {
		this(NewYorkBusDataReader.STOP_CENTROID_SEMANTIC, lines);
	}
	
	public NewYorkBusProblem(StopSemantic stopSemantic, String... lines) {
		this(stopSemantic, false, lines);
	}
	
	public NewYorkBusProblem(StopSemantic stopSemantic, boolean onlyStops, String... lines) {
		super(stopSemantic);
		this.onlyStops = onlyStops;
		this.lines = lines;
	}
	
	@Override
	public Semantic discriminator() {
		return NewYorkBusDataReader.ROUTE;
	}
	
	@Override
	public String shortDescripton() {
		return "New York bus" + (lines != null ? "(lines=" + ArrayUtils.toString(lines) + ")" : "") + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new NewYorkBusDataReader(onlyStops).read(lines));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new NewYorkBusDatabaseReader(onlyStops).read(lines));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
