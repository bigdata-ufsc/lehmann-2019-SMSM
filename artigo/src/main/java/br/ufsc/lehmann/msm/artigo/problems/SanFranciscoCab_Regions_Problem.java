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

public class SanFranciscoCab_Regions_Problem extends SanFranciscoCabProblem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private Random random = new Random();
	private String[] regions;
	private String[] directions;
	private String[] roads;
	private StopMoveStrategy strategy;

	public SanFranciscoCab_Regions_Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, String[] roads, String[] directions, String[] regions, boolean onlyStop) {
		super(stopSemantic, onlyStop);
		this.strategy = strategy;
		if(ArrayUtils.isEmpty(roads) && ArrayUtils.isEmpty(directions) && ArrayUtils.isEmpty(regions)) {
			throw new IllegalArgumentException();
		}
		this.roads = roads;
		this.directions = directions;
		this.regions = regions;
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
		if(!ArrayUtils.isEmpty(directions)) {
			if(!ArrayUtils.isEmpty(roads)) {
				if(!ArrayUtils.isEmpty(regions)) {
					return SanFranciscoCabDataReader.ROUTE_IN_ROADS_WITH_DIRECTION;
				}
				return SanFranciscoCabDataReader.ROADS_WITH_DIRECTION;
			}
			if(!ArrayUtils.isEmpty(regions)) {
				return SanFranciscoCabDataReader.ROUTE_WITH_DIRECTION;
			}
			return SanFranciscoCabDataReader.DIRECTION;
		}
		if(!ArrayUtils.isEmpty(roads)) {
			if(!ArrayUtils.isEmpty(regions)) {
				return SanFranciscoCabDataReader.ROUTE_WITH_ROADS;
			}
			return SanFranciscoCabDataReader.ROAD;
		}
		if(!ArrayUtils.isEmpty(regions)) {
			return SanFranciscoCabDataReader.ROUTE;
		}
		return null;
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
		return "San Francisco cab (" + (!ArrayUtils.isEmpty(directions) ? "Directed " : "") + "Regions)[" + getStopSemantic().name() + "][onlyStops=" + onlyStop + "]";
	}
	
	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new SanFranciscoCabDataReader(onlyStop, strategy, roads, directions, regions).read());
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}

//		data = data.stream()//
//				.filter(t -> Arrays.asList(
//						459330,175302,347277,564396,525373,// (a->m/101)
//						374700,614527,73718,176231,771453,// (a->m/280)
//						409260,484589,768378,418507,461179,// (m->a/101)
//						431550,59000,801975,595185,983724// (m->a/280)
//						).contains(t.getTrajectoryId()))//
//				.sorted((o1, o2) -> ((Comparable) o1.getTrajectoryId()).compareTo(o2.getTrajectoryId()))//
//				.collect(Collectors.toList());
//		try {
//			data = new ArrayList<>(new SanFranciscoCabDatabaseReader(onlyStop, roads, directions, regions).read());
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
