package br.ufsc.lehmann.msm.artigo;

import java.util.ArrayList;
import java.util.List;

public enum Climate {

	CLEAR("no value"), FOG("fog"), MODERATE_RAIN("mod rain"), LITTLE_RAIN("lt rain"), HAZE("haze");
	
	private String name;

	Climate(String name) {
		this.name = name;
	}

	public static Climate[] parseClimates(String value) {
		if(value == null || value.isEmpty()) {
			return null;
		}
		Climate[] values = Climate.values();
		List<Climate> ret = new ArrayList<>(values.length);
		for (int i = 0; i < values.length; i++) {
			if(values[i].name.equals(value)) {
				ret.add(values[i]);
			}
		}
		if(!ret.isEmpty()) {
			return ret.toArray(new Climate[ret.size()]);
		}
		return null;
	}
}
