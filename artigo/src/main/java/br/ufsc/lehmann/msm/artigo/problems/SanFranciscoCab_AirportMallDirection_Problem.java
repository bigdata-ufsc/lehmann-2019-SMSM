package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public class SanFranciscoCab_AirportMallDirection_Problem extends SanFranciscoCabProblem {
	
	private String[] roads;
	private String[] directions;

	public SanFranciscoCab_AirportMallDirection_Problem(String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, roads);
	}

	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, String[] roads) {
		this(SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, false, roads);
	}
	
	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, boolean onlyStop, String[] roads) {
		this(stopSemantic, onlyStop, roads, null);
	}

	public SanFranciscoCab_AirportMallDirection_Problem(StopSemantic stopSemantic, boolean onlyStop, String[] roads, String[] directions) {
		super(stopSemantic, onlyStop);
		this.roads = roads;
		/**
		 * Proporção das classes de direction
		 * 	"airport to mall";106
		 *	"mall to airport";203
		 *	"";217
		 */
		this.directions = directions;
	}
	@Override
	public Semantic discriminator() {
		if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.isEmpty(directions)) {
			return SanFranciscoCabDataReader.ROADS_WITH_DIRECTION;
		}
		return SanFranciscoCabDataReader.DIRECTION;
	}

	@Override
	public String shortDescripton() {
		if(!ArrayUtils.isEmpty(roads) && !ArrayUtils.isEmpty(directions)) {
			return "San Francisco cab (Airport <-> Mall|Direction|" + cc.mallet.util.ArrayUtils.toString(roads) + ")[" + stopSemantic().name() + "][onlyStops=" + onlyStop + "]";
		}
		return "San Francisco cab (Airport <-> Mall|Direction)[" + stopSemantic().name() + "][onlyStops=" + onlyStop + "]";
	}
	
	protected List<SemanticTrajectory> load() {
//		try {
//			return new ArrayList<>(new SanFranciscoCabDataReader(onlyStop, roads, directions).read());
//		} catch (IOException | ParseException e) {
//			throw new RuntimeException(e);
//		}
		try {
			return new ArrayList<>(new SanFranciscoCabDatabaseReader(onlyStop, roads, directions).read());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
