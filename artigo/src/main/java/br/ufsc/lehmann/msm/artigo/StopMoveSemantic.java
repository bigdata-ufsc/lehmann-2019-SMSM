package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.core.trajectory.semantic.StopMove;
import br.ufsc.lehmann.MoveSemantic;

public class StopMoveSemantic extends Semantic<StopMove, Number> {

	private AttributeDescriptor<StopMove, ? extends Object> desc;
	private StopSemantic stopSemantic;
	private MoveSemantic moveSemantic;

	public StopMoveSemantic(StopSemantic stopSemantic, MoveSemantic moveSemantic, AttributeDescriptor<StopMove, ? extends Object> desc) {
		super(-1);
		this.stopSemantic = stopSemantic;
		this.moveSemantic = moveSemantic;
		this.desc = desc;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return distance(getData(a, i), getData(b, j));
	}

	@Override
	public double distance(StopMove d1, StopMove d2) {
		if (d1 == d2) {
			return 0;
		}
		if (d1 == null || d2 == null) {
			return Double.MAX_VALUE;
		}
		return desc.distance(d1, d2);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
		return match(getData(a, i), getData(b, j), threshold);
	}

	public boolean match(StopMove d1, StopMove d2, Number threshold) {
		double distance = distance(d1, d2);
		if (threshold == null) {
			return distance == 0;
		}
		return distance <= desc.convertThreshold(threshold.doubleValue());
	}
	
	@Override
	public StopMove getData(SemanticTrajectory p, int i) {
		Object data = stopSemantic.getData(p, i);
		if(data != null) {
			return new StopMove((Stop) data);
		}
		data = moveSemantic.getData(p, i);
		if (data != null) {
			return new StopMove((Move) data);
		}
		return null;
	}
	
	public String name() {
		return desc.attributeName();
	}

}
