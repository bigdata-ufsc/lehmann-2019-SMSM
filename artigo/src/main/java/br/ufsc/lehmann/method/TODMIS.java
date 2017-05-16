package br.ufsc.lehmann.method;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class TODMIS extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private List<SemanticTrajectory> trajectories;

	public TODMIS(List<SemanticTrajectory> trajectories, TODMISSemanticParameter<?, ?> semantic) {
		this.trajectories = trajectories;
		Map<Object, Map<Object, Double>> probabilities = new HashMap<>();
		Map<Object, Map<Object, Double>> timeSpent = new HashMap<>();
		//
		Map<Object, Map<Object, Integer>> counters = new HashMap<>();
		double transitionsCount = 0.0;
		for (int j = 0; j < trajectories.size(); j++) {
			SemanticTrajectory traj = trajectories.get(j);
			//<temporal intervals>
			Map<Object, Map<Object, Double>> trajTimeSpent = new HashMap<>();
			for (int i = 0; i < traj.length(); i++) {
				Object semanticData = semantic.semantic.getData(traj, i);
				TemporalDuration date = Semantic.TEMPORAL.getData(traj, i);
				Map<Object, Double> semanticTimeSpent = trajTimeSpent.get(semanticData);
				if(semanticTimeSpent == null) {
					semanticTimeSpent = new HashMap<>();
					trajTimeSpent.put(semanticData, semanticTimeSpent);
				}
				Double time = semanticTimeSpent.get(semanticData);
				if(time == null) {
					time = (double) date.getEnd().toEpochMilli() - date.getStart().toEpochMilli();
				} else {
					time += (double) date.getEnd().toEpochMilli() - date.getStart().toEpochMilli();
				}
				semanticTimeSpent.put(semanticData, time);
			}
			//</temporal intervals>
			//<Markov state transitions>
			for (int i = 0; i < traj.length() - 1; i++) {
				Object semanticDataBegin = semantic.semantic.getData(traj, i);
				Object semanticDataEnd = semantic.semantic.getData(traj, i + 1);
				Map<Object, Integer> p = counters.get(semanticDataBegin);
				if(p == null) {
					p = new HashMap<>();
					counters.put(semanticDataBegin, p);
				}
				//qual a probabilidade de ele visitar tal stop?
				Integer prob = p.get(semanticDataBegin);
				if(prob == null) {
					prob = 0;
					transitionsCount++;
				}
				prob += 1;
				p.put(semanticDataBegin, prob);
				//qual a probabilidade de ele ir do stop Start para o End?
				prob = p.get(semanticDataEnd);
				if(prob == null) {
					prob = 0;
					transitionsCount++;
				}
				prob += 1;
				p.put(semanticDataEnd, prob);
				//<temporal intervals>
				TemporalDuration dateBegin = Semantic.TEMPORAL.getData(traj, i);
				TemporalDuration dateEnd = Semantic.TEMPORAL.getData(traj, i + 1);
				Map<Object, Double> semanticTimeSpent = trajTimeSpent.get(semanticDataBegin);
				if(semanticTimeSpent == null) {
					semanticTimeSpent = new HashMap<>();
					trajTimeSpent.put(semanticDataBegin, semanticTimeSpent);
				}
				Double time = semanticTimeSpent.get(semanticDataEnd);
				if(time == null) {
					time = (double) dateBegin.getEnd().toEpochMilli() - dateEnd.getStart().toEpochMilli();
				} else {
					time += (double) dateBegin.getEnd().toEpochMilli() - dateEnd.getStart().toEpochMilli();
				}
				semanticTimeSpent.put(semanticDataEnd, time);
				//</temporal intervals>
			}
			//</Markov state transitions>
			//<temporal intervals>
			double totalTime = 0.0;
			for (Iterator iterator = trajTimeSpent.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Object, Map<Object, Double>> semanticTrajectory = (Map.Entry<Object, Map<Object, Double>>) iterator.next();
				for (Iterator it = semanticTrajectory.getValue().entrySet().iterator(); it.hasNext();) {
					Map.Entry<Object, Double> time = (Map.Entry<Object, Double>) iterator.next();
					totalTime += time.getValue();
				}
			}
			for (Iterator iterator = trajTimeSpent.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Object, Map<Object, Double>> semanticTrajectory = (Map.Entry<Object, Map<Object, Double>>) iterator.next();
				Map<Object, Double> value = semanticTrajectory.getValue();
				for (Iterator it = value.keySet().iterator(); it.hasNext();) {
					Object destin = iterator.next();
					value.put(destin, value.get(destin).doubleValue() / totalTime);
				}
			}
			//</temporal intervals>
		}
		//<Markov state transitions>
		for (Iterator iterator = counters.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Object, Map<Object, Integer>> counter = (Map.Entry<Object, Map<Object, Integer>>) iterator.next();
			Map<Object, Double> p = probabilities.get(counter.getKey());
			if(p == null) {
				p = new HashMap<>();
				probabilities.put(counter.getKey(), p);
			}
			Map<Object, Integer> values = counter.getValue();
			for (Iterator it = values.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Object, Integer> tr = (Map.Entry<Object, Integer>) it.next();
				p.put(tr.getKey(), tr.getValue()/ transitionsCount);
			}
		}
		//</Markov state transitions>
	}
	
	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static class TODMISSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;

		public TODMISSemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}

}
