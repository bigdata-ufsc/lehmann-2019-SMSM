package br.ufsc.lehmann.testexecution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.gson.Gson;

import br.ufsc.core.ComputableThreshold;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.lehmann.ComputableDouble;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix.Tuple;

public class GridSearchParams {
	
	private boolean firstExecution;
	private LinkedList<Config> configurations = new LinkedList<>();
	private List<Object> register = new ArrayList<>();

	public GridSearchParams() {
		this.firstExecution = true;
	}

	public boolean hasNextConfigurations() {
		if(firstExecution) {
			firstExecution = false;
			return true;
		}
		if(configurations.isEmpty()) {
			return false;
		}
		Optional<Config> hasConfig = configurations.stream().filter(c -> c.params.stream().allMatch(t -> !t.getLast().used)).findFirst();
		
		return hasConfig.isPresent();
	}

	public Number getThreshold(Param param) {
		String threshold = param.getThreshold();
		if(threshold == null) {
			return null;
		}
		try {
			return Double.parseDouble(threshold);
		} catch (NumberFormatException e) {
		}
		
		Value ret = registerValues(param, threshold);
		return (Number) ret.value;
	}

	public Number getConfig(Map<String, String> cfg, String propoerty) {
		String threshold = cfg.get(propoerty);
		if(threshold == null) {
			return null;
		}
		try {
			return Double.parseDouble(threshold);
		} catch (NumberFormatException e) {
		}
		
		Value ret = registerValues(cfg, threshold);
		return (Number) ret.value;
	}

	private Value registerValues(Object param, String threshold) {
		Value ret = null;
		if(!configurations.isEmpty()) {
			if(register.contains(param)) {
				Optional<Config> hasConfig = configurations.stream().filter(c -> c.params.stream().filter(t -> t.getFirst() == param && !t.getLast().used).findFirst().isPresent()).findFirst();
				Tuple<Object, Value> tuple = hasConfig.get().params.stream().filter(t -> t.getFirst() == param).findFirst().get();
				ret = tuple.getLast();
				ret.used = true;
			} else {
				register.add(param);
				Gson gson = new Gson();
				String[] multipleThresholds = gson.fromJson(threshold, String[].class);
				LinkedList<Config> newConfigurations = new LinkedList<>();
				for (String t : multipleThresholds) {
					Number value = toNumber(t);
					for (Config config2 : configurations) {
						Config cp = config2.copy();
						cp.params.stream().forEach(tu -> tu.getLast().used = false);
						cp.params.add(new Tuple<Object, Value>(param, new Value(value, false)));
						newConfigurations.add(cp);
					}
				}
				this.configurations = newConfigurations;
				Config config = configurations.getFirst();
				config.params.stream().forEach(t -> t.getLast().used = true);
				
				ret = new Value(toNumber(multipleThresholds[0]), true);
			}
		} else {
			register.add(param);
			Gson gson = new Gson();
			String[] multipleThresholds = gson.fromJson(threshold, String[].class);
			LinkedList<Config> newConfigurations = new LinkedList<>();
			for (String t : multipleThresholds) {
				Number value = toNumber(t);
				Config cp = new Config();
				cp.params.add(new Tuple<Object, Value>(param, new Value(value, false)));
				newConfigurations.add(cp);
			}
			this.configurations = newConfigurations;
			Config config = configurations.getFirst();
			Optional<Tuple<Object, Value>> hasConfig = config.params.stream().filter(t -> t.getFirst() == param).findFirst();

			Tuple<Object, Value> tuple = hasConfig.get();
			ret = tuple.getLast();
			ret.used = true;
		}
		return ret;
	}

	private Number toNumber(String t) {
		Number value = 0;
		try {
			value = Double.parseDouble(t);
		} catch (NumberFormatException e) {
			if(t.startsWith("summed-distances")) {
				String[] expression = t.split("\\*");
				Number DTWmultiplier = expression.length > 1 ? Double.parseDouble(expression[1]) : 1L;
				value = new ComputableDouble<Move>() {
					public Number compute(Move a, Move b) {
						return (a.getTravelledDistance() + b.getTravelledDistance()) * DTWmultiplier.doubleValue();
					}
				};
			} else if(t.startsWith("std-lcss")) {
				Matcher m = Pattern.compile("std-lcss\\((.+)+\\)").matcher(t);
				if(m.matches()) {
					String stdParam = m.group(1);
					Number stdValue = computeMath(stdParam);
					value = new ComputableThreshold<Number, Object>(t) {

						@Override
						public Number compute(Object a, Object b, SemanticTrajectory trajA, SemanticTrajectory trajB,
								Semantic<Object, Number> semantic) {
							double stdA = getStandardDeviation(trajA, semantic);
							double stdB = getStandardDeviation(trajB, semantic);
							return Math.min(stdA, stdB) * stdValue.doubleValue();
						}

						private double getStandardDeviation(SemanticTrajectory traj,
								Semantic<Object, Number> semantic) {
							return traj.getLocalStats(semantic).getStandardDeviation();
						}
					};
				}
			} else if(t.startsWith("std-edr")) {
				Matcher m = Pattern.compile("std-edr\\((.+)+\\)").matcher(t);
				if(m.matches()) {
					String stdParam = m.group(1);
					Number stdValue = computeMath(stdParam);
					value = new ComputableThreshold<Number, Object>(t) {

						@Override
						public Number compute(Object a, Object b, SemanticTrajectory trajA, SemanticTrajectory trajB,
								Semantic<Object, Number> semantic) {
							return getStandardDeviation(trajA, semantic) * stdValue.doubleValue();
						}

						private double getStandardDeviation(SemanticTrajectory traj,
								Semantic<Object, Number> semantic) {
							return traj.getGlobalStats(semantic).getStandardDeviation();
						}
					};
				}
			} else {
				throw new IllegalArgumentException("Unexpected threshold value: " + t, e);
			}
		}
		return value;
	}

	private static Number computeMath(String stdParam) {
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			Number stdValue = (Number) engine.eval(stdParam);
			return stdValue;
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Config {
		List<Tuple<Object, Value>> params = new LinkedList<>();
		
		Config() {
		}
		
		public Config(List<Tuple<Object, Value>> collect) {
			params = collect;
		}

		Config copy() {
			return new Config(params.stream().map(t -> new Tuple<>(t.getFirst(), t.getLast().copy())).collect(Collectors.toList()));
		}
	}
	
	private static class Value {
		Object value;
		boolean used;
		public Value(Object value, boolean used) {
			this.value = value;
			this.used = used;
		}
		Value copy() {
			return new Value(value, used);
		}
	}
	
	public static void main(String[] args) {
		GridSearchParams params = new GridSearchParams();
		Param p = new Param();
		Param q = new Param();
		Param o = new Param();
		p.setThreshold("[1,2,3]");
		q.setThreshold("[10,20,30]");
		o.setThreshold("[100,200,300]");
		while(params.hasNextConfigurations()) {
			System.out.println(params.getThreshold(p));
			System.out.println(params.getThreshold(q));
			System.out.println(params.getThreshold(o));
			System.out.println("--------------------");
		}
	}
}
