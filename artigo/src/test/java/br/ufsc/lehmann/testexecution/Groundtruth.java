
package br.ufsc.lehmann.testexecution;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Groundtruth {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("index")
    @Expose
    private Long index;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}

}
