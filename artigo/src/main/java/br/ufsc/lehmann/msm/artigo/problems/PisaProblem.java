package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class PisaProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private Random random = new Random();
	private boolean onlyStops;
	private StopSemantic stopSemantic;
	private Integer[] users;

	public PisaProblem(Integer... users) {
		this(false, users);
	}

	public PisaProblem(boolean onlyStops, Integer... users) {
		this(PisaDataReader.STOP_CENTROID_SEMANTIC, onlyStops, users);
	}

	public PisaProblem(StopSemantic stopSemantic,boolean onlyStops, Integer... users) {
		this.stopSemantic = stopSemantic;
		this.onlyStops = onlyStops;
		this.users = users;
	}
	
	@Override
	public void initialize(Random r) {
		if(!random.equals(r)) {
			random = r;
			loaded = false;
			load();
		}
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
		return PisaDataReader.USER_ID;
	}

	public StopSemantic stopSemantic() {
		return stopSemantic;
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
		return "Pisa [" + stopSemantic.name() + "][onlyStops=" + onlyStops + "]" + (users != null ? "(Users " + Arrays.toString(users) + ")" : "");
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new PisaDataReader(onlyStops).read(users));
		} catch (NumberFormatException | ParseException | IOException e) {
			throw new RuntimeException(e);
		}
//		try {
//			data = new ArrayList<>(new PisaDatabaseReader(onlyStops).read(users));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
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

}
