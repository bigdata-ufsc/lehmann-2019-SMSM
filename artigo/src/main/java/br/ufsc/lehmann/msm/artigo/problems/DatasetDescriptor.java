package br.ufsc.lehmann.msm.artigo.problems;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DatasetDescriptor {


    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("header")
    @Expose
    private List<String> header;
    @SerializedName("data_files")
    @Expose
	private List<String> data_files;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getHeader() {
		return header;
	}
	public void setHeader(List<String> header) {
		this.header = header;
	}
	public List<String> getData_files() {
		return data_files;
	}
	public void setData_files(List<String> data_files) {
		this.data_files = data_files;
	}
}
