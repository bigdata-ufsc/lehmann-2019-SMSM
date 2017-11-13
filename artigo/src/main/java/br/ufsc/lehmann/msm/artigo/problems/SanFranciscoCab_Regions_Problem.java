package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public class SanFranciscoCab_Regions_Problem extends SanFranciscoCabProblem {
	
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
	public String shortDescripton() {
		return "San Francisco cab (" + (!ArrayUtils.isEmpty(directions) ? "Directed " : "") + "Regions)[" + stopSemantic().name() + "][onlyStops=" + onlyStop + "]";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new SanFranciscoCabDataReader(onlyStop, strategy, roads, directions, regions).read());
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
	}

}
