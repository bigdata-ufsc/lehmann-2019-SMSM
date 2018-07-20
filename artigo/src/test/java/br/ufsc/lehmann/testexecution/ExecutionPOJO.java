
package br.ufsc.lehmann.testexecution;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExecutionPOJO {

    @SerializedName("dataset")
    @Expose
    private Dataset dataset;
    @SerializedName("measure")
    @Expose
    private Measure measure;
    @SerializedName("groundtruth")
    @Expose
    private Groundtruth groundtruth;

	public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public Groundtruth getGroundtruth() {
		return groundtruth;
	}

	public void setGroundtruth(Groundtruth groundtruth) {
		this.groundtruth = groundtruth;
	}

}
