package br.ufsc.lehmann;

import java.time.Instant;

import br.ufsc.core.trajectory.OverlapTemporalSemantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TemporalDuration;

public class SlackTemporalSemantic extends OverlapTemporalSemantic {
	
	public static final SlackTemporalSemantic SLACK_TEMPORAL = new SlackTemporalSemantic(2);
	private Number slack;

	public SlackTemporalSemantic(int index) {
		this(index, new ComputableDouble<Object>() {
			public Number compute(Object a, Object b) {
				return Thresholds.SLACK_TEMPORAL;
			}
		});
	}
	
	public SlackTemporalSemantic(int index, Number slack) {
		super(index);
		this.slack = slack;
		
	}
	
	@Override
	public boolean match(TemporalDuration d1, TemporalDuration d2, Number threshlod) {
		d1 = new TemporalDuration(Instant.ofEpochMilli(d1.getStart().toEpochMilli() - slack.intValue()/2), Instant.ofEpochMilli(d1.getEnd().toEpochMilli() +  slack.intValue()/2));
		d2 = new TemporalDuration(Instant.ofEpochMilli(d2.getStart().toEpochMilli() - slack.intValue()/2), Instant.ofEpochMilli(d2.getEnd().toEpochMilli() +  slack.intValue()/2));
		return super.match(d1, d2, threshlod);
	}
	
	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		TemporalDuration d1 = (TemporalDuration) a.getDimensionData(index, i);
		TemporalDuration d2 = (TemporalDuration) b.getDimensionData(index, j);
		return this.match(d1, d2, threshlod);
	}
}
