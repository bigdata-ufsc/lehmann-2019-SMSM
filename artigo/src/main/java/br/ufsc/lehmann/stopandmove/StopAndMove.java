package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;

public class StopAndMove {

	private Multimap<Stop, Integer> stops = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	private Multimap<Move, Integer> moves = MultimapBuilder.linkedHashKeys().hashSetValues().build();

	public List<Stop> getStops() {
		return new ArrayList<>(stops.keySet());
	}

	public void mergeStops(Stop s1, Stop s2) {
		s1.setEndTime(s2.getEndTime());
		double cx = (s1.getCentroid().getX() + s2.getCentroid().getX()) / 2;
		double cy = (s1.getCentroid().getY() + s2.getCentroid().getY()) / 2;

		TPoint c = new TPoint(0, cx, cy, new Timestamp(0));
		s1.setCentroid(c);
		
		stops.putAll(s1, stops.removeAll(s2));
	}

	public Collection<Integer> remove(Stop s) {
		return stops.removeAll(s);
	}

	public void addMove(Move move, Collection<Integer> gids) {
		moves.putAll(move, gids);
	}

	public void addStop(Stop s, Collection<Integer> gids) {
		stops.putAll(s, gids);
	}

	public List<Integer> getGids(Stop stop) {
		return new ArrayList<>(stops.get(stop));
	}
}
