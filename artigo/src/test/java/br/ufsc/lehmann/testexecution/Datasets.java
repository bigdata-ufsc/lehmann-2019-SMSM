package br.ufsc.lehmann.testexecution;

import com.google.gson.Gson;

import br.ufsc.lehmann.msm.artigo.problems.Geolife2DatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;

public class Datasets {

	public static IDataReader createDataset(Dataset dataset) {
		if(dataset.getName().equalsIgnoreCase("involves")) {
			Boolean weekly = Boolean.valueOf(dataset.getParams().get("weekly"));
			String pointsTableSuffix = dataset.getParams().get("pointsTableSuffix");
			String stopMoveTableSuffix = dataset.getParams().get("stopMoveTableSuffix");
			return new InvolvesDatabaseReader(!dataset.getRaw(), weekly, pointsTableSuffix, stopMoveTableSuffix);
		}
		if(dataset.getName().equalsIgnoreCase("geolife")) {
			Boolean withTransportation = Boolean.valueOf(dataset.getParams().get("withTransportation"));
			String pointsTable = dataset.getParams().get("pointsTable");
			String moveTable = dataset.getParams().get("moveTable");
			String stopTable = dataset.getParams().get("stopTable");
			return new GeolifeDatabaseReader(!dataset.getRaw(), withTransportation, stopTable, moveTable, pointsTable);
		}
		if(dataset.getName().equalsIgnoreCase("geolife_university")) {
			String pointsTable = dataset.getParams().get("pointsTable");
			String moveTable = dataset.getParams().get("moveTable");
			String stopTable = dataset.getParams().get("stopTable");
			return new GeolifeUniversityDatabaseReader(!dataset.getRaw(), stopTable, moveTable, pointsTable);
		}
		if(dataset.getName().equalsIgnoreCase("geolife2")) {
			String pointsTable = dataset.getParams().get("pointsTable");
			String moveTable = dataset.getParams().get("moveTable");
			String stopTable = dataset.getParams().get("stopTable");
			String mappingTable = dataset.getParams().get("mappingTable");
			return new Geolife2DatabaseReader(!dataset.getRaw(), stopTable, moveTable, mappingTable, pointsTable);
		}
		if(dataset.getName().equalsIgnoreCase("crawdad")) {
			Gson gson = new Gson();
			String[] roads = gson.fromJson(dataset.getParams().get("roads"), String[].class);
			String[] directions = gson.fromJson(dataset.getParams().get("directions"), String[].class);
			String[] regions = gson.fromJson(dataset.getParams().get("regions"), String[].class);
			return new SanFranciscoCabDatabaseReader(!dataset.getRaw(), roads, directions, regions);
		}
		return null;
	}

}
