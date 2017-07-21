package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class PatelProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private String table;
	private String stopMoveTable;
	private boolean loaded;
	private Random random = new Random();

	public PatelProblem(String table) {
		this(table, table);
	}

	public PatelProblem(String dataTable, String stopMoveTable) {
		this.table = dataTable;
		this.stopMoveTable = stopMoveTable;
	}

	public PatelProblem(String table, String stopMoveTable, Random random) {
		this(table, stopMoveTable);
		this.random = random;
	}

	@Override
	public Problem clone(Random r) {
		return new PatelProblem(table, stopMoveTable, r);
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new PatelDataReader(table, stopMoveTable).read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		Collections.shuffle(data, new java.util.Random() {
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		this.loaded = true;
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
			 Semantic.GEOGRAPHIC, //
			 Semantic.TEMPORAL,//
				PatelDataReader.STOP_SEMANTIC,//
				PatelDataReader.MOVE_SEMANTIC
		};
	}

	@Override
	public List<SemanticTrajectory> data() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public Semantic discriminator() {
		return PatelDataReader.CLASS;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			load();
		}
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			load();
		}
		return testingData;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			load();
		}
		return validatingData;
	}

	@Override
	public String shortDescripton() {
		return "Patel's " + StringUtils.capitalize(table);
	}

}
