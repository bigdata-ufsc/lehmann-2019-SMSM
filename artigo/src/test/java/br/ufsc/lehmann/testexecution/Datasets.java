package br.ufsc.lehmann.testexecution;

import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;

public class Datasets {

	public static IDataReader createDataset(Dataset dataset) {
		if(dataset.getName().equalsIgnoreCase("involves")) {
			Boolean weekly = Boolean.valueOf(dataset.getParams().get("weekly"));
			String pointsTableSuffix = dataset.getParams().get("pointsTableSuffix");
			String stopMoveTableSuffix = dataset.getParams().get("stopMoveTableSuffix");
			return new InvolvesDatabaseReader(!dataset.getRaw(), weekly, pointsTableSuffix, stopMoveTableSuffix);
		}
		return null;
	}

}
