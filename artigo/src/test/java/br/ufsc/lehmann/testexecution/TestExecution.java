package br.ufsc.lehmann.testexecution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;

public class TestExecution {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		System.out.println(new File("./").getAbsolutePath());
		ExecutionPOJO execution = new Gson().fromJson(new FileReader("./src/test/resources/executions/teste.test"), ExecutionPOJO.class);
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		IDataReader dataReader = Datasets.createDataset(dataset);
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);
	}
	
}
