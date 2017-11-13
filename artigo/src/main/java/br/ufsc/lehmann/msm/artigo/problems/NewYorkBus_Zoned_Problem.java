package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import cc.mallet.util.ArrayUtils;

public class NewYorkBus_Zoned_Problem extends NewYorkBusProblem {
	
	private String[] zones;
	private boolean onlyStops;
	private boolean withDirection;
	private StopMoveStrategy strategy;
	
	public NewYorkBus_Zoned_Problem(String... lines) {
		this(NewYorkBus_Zoned_DatabaseReader.STOP_CENTROID_SEMANTIC, lines);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, String... zones) {
		this(stopSemantic, false, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, boolean onlyStops, String... zones) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, String... zones) {
		this(stopSemantic, strategy, onlyStops, false, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, boolean withDirection, String... zones) {
		super(stopSemantic);
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.withDirection = withDirection;
		this.zones = zones;
	}

	@Override
	public Semantic discriminator() {
		if(withDirection) {
			return NewYorkBusDataReader.ROUTE_WITH_DIRECTION;
		}
		return NewYorkBusDataReader.ROUTE;
	}

	@Override
	public String shortDescripton() {
		return "New York bus " + (withDirection ? "Directed " : "") + (!org.apache.commons.lang3.ArrayUtils.isEmpty(zones)? "(zones=" + ArrayUtils.toString(zones) + ")" : "") + "[" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new NewYorkBusDataReader(onlyStops, strategy).read(zones));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
//		try {
//			return new ArrayList<>(new NewYorkBus_Zoned_DatabaseReader(onlyStops).read(zones));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

}
