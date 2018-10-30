
package br.ufsc.lehmann.testexecution;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Measure {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("optimizer")
    @Expose
    private String optimizer;
    @SerializedName("params")
    @Expose
    private List<Param> params = null;
    @SerializedName("config")
    @Expose
    private Map<String, String> config = null;

    public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getOptimizer() {
		return optimizer;
	}
	public void setOptimizer(String optimizer) {
		this.optimizer = optimizer;
	}
}
