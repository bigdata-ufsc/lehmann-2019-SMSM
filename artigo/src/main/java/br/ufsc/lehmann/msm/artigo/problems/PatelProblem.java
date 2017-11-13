package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;
import smile.math.Random;

public class PatelProblem extends AbstractProblem {
	
	private String table;
	private String stopMoveTable;
	private boolean onlyStops;

	public PatelProblem(String table) {
		this(table, table);
	}

	public PatelProblem(String dataTable, String stopMoveTable) {
		this(PatelDataReader.STOP_CENTROID_SEMANTIC, dataTable, stopMoveTable);
	}

	public PatelProblem(StopSemantic stopSemantic, String table, String stopMoveTable) {
		this(stopSemantic, false, table, stopMoveTable);
	}

	public PatelProblem(StopSemantic stopSemantic, boolean onlyStops, String table, String stopMoveTable) {
		super(stopSemantic);
		this.onlyStops = onlyStops;
		this.table = table;
		this.stopMoveTable = stopMoveTable;
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new PatelDataReader(onlyStops, table, stopMoveTable).read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Semantic discriminator() {
		return PatelDataReader.CLASS;
	}
	
	@Override
	public String shortDescripton() {
		return "Patel's " + StringUtils.capitalize(table) + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}

}
