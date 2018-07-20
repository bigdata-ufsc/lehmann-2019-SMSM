
package br.ufsc.lehmann.testexecution;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Param {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("distance")
    @Expose
    private String distance;
    @SerializedName("threshold")
    @Expose
    private String threshold;
    @SerializedName("weight")
    @Expose
    private Double weight;
    @SerializedName("index")
    @Expose
    private Long index;
    @SerializedName("params")
    @Expose
    private List<Param> params = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}

}
