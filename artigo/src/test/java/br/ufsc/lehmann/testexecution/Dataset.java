
package br.ufsc.lehmann.testexecution;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dataset {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("raw")
    @Expose
    private Boolean raw;
    @SerializedName("min-trajectories-per-class")
    @Expose
    private Long minTrajectoriesPerClass;
	@SerializedName("params")
    @Expose
    private Map<String, String> params = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Boolean getRaw() {
		return raw;
	}

	public void setRaw(Boolean raw) {
		this.raw = raw;
	}

	public Long getMinTrajectoriesPerClass() {
		return minTrajectoriesPerClass;
	}

	public void setMinTrajectoriesPerClass(Long minTrajectoriesPerClass) {
		this.minTrajectoriesPerClass = minTrajectoriesPerClass;
	}

}
