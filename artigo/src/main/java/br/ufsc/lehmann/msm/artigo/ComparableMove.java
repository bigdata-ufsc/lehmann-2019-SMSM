package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.semantic.Move;

public class ComparableMove extends Move implements Comparable<ComparableMove> {
	
	private MoveComparator comparator;

	public ComparableMove(Move move, MoveComparator comparator) {
		super(move.getMoveId(), move.getStart(), move.getEnd(), move.getStartTime(), move.getEndTime(), move.getBegin(), move.getLength(), move.getAngle());
		this.comparator = comparator;
	}

	@Override
	public int compareTo(ComparableMove o) {
		return comparator.compare(this, o);
	}

}
