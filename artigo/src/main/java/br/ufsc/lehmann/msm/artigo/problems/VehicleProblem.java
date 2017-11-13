package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class VehicleProblem extends AbstractProblem {
	
	private boolean onlyStops;

	public VehicleProblem() {
		this(VehicleDataReader.STOP_CENTROID_SEMANTIC);
	}

	public VehicleProblem(StopSemantic stopSemantic) {
		this(stopSemantic, false);
	}

	public VehicleProblem(StopSemantic stopSemantic, boolean onlyStops) {
		super(stopSemantic);
		this.onlyStops = onlyStops;
	}
	
	protected List<SemanticTrajectory> load() {
		try {
			return new ArrayList<>(new VehicleDataReader(onlyStops).read());
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Semantic discriminator() {
		return VehicleDataReader.CLASS;
	}
	
	@Override
	public String shortDescripton() {
		return "Vehicle [" + stopSemantic().name() + "][onlyStops=" + onlyStops + "]";
	}

}
