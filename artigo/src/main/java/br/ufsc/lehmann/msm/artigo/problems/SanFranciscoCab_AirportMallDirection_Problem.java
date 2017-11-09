package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import smile.math.Random;

public class SanFranciscoCab_AirportMallDirection_Problem extends SanFranciscoCabProblem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private Random random = new Random();
	private String[] roads;
	private String[] directions;

	public SanFranciscoCab_AirportMallDirection_Problem(String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, roads);
	}

	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, false, roads);
	}
	
	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, boolean onlyStop, String[] roads) {
		super(stopSemantic, onlyStop);
		this.roads = roads;
	}

	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, boolean onlyStop, String[] roads, String[] directions) {
		this(stopSemantic, onlyStop, roads);
		/**
		 * Proporção das classes de direction
		 * 	"airport to mall";106
		 *	"mall to airport";203
		 *	"";217
		 */
		this.directions = directions;
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
		if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.isEmpty(directions)) {
			return SanFranciscoCabDataReader.ROADS_WITH_DIRECTION;
		}
		return SanFranciscoCabDataReader.DIRECTION;
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
		if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.isEmpty(directions)) {
			return "San Francisco cab (Airport <-> Mall|Direction|" + cc.mallet.util.ArrayUtils.toString(roads) + ")[" + getStopSemantic().name() + "][onlyStops=" + onlyStop + "]";
		}
		return "San Francisco cab (Airport <-> Mall|Direction)[" + getStopSemantic().name() + "][onlyStops=" + onlyStop + "]";
	}
	
	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new SanFranciscoCabDataReader(onlyStop, roads, directions).read());
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
//		try {
//			data = new ArrayList<>(new SanFranciscoCabDatabaseReader(onlyStop, roads).read());
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
//		data = data.subList(0, data.size() / 80);
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		loaded = true;
	}

}
