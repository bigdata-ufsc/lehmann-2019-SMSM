package br.ufsc.lehmann.testexecution;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import br.ufsc.lehmann.msm.artigo.loader.AISBrestDataReader;
import br.ufsc.lehmann.msm.artigo.loader.AnimalsSTARKEYDataReader;
import br.ufsc.lehmann.msm.artigo.loader.FoursquareDataReader;
import br.ufsc.lehmann.msm.artigo.loader.GeolifeTransportationModeDataReader;
import br.ufsc.lehmann.msm.artigo.loader.HASLDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.CRAWDADDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.Geolife2DatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisDataReader;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.StopMoveStrategy;

public class Datasets {

	public static IDataReader createDataset(Dataset dataset) {
		Map<String, String> p = dataset.getParams();
		if(dataset.getName().equalsIgnoreCase("involves")) {
			Boolean weekly = Boolean.valueOf(p.get("weekly"));
			String pointsTableSuffix = p.get("pointsTableSuffix");
			String stopMoveTableSuffix = p.get("stopMoveTableSuffix");
			return new InvolvesDatabaseReader(!dataset.getRaw(), weekly, pointsTableSuffix, stopMoveTableSuffix);
		}
		if(dataset.getName().equalsIgnoreCase("geolife")) {
			Boolean withTransportation = Boolean.valueOf(p.get("withTransportation"));
			String pointsTable = p.get("pointsTable");
			String moveTable = p.get("moveTable");
			String stopTable = p.get("stopTable");
			return new GeolifeDatabaseReader(!dataset.getRaw(), withTransportation, stopTable, moveTable, pointsTable);
		}
		if(dataset.getName().equalsIgnoreCase("geolife_university")) {
			Boolean normalized = Boolean.FALSE;
			if(p != null) {
				Gson gson = new Gson();
				String json = p.get("normalized");
				if(!StringUtils.isEmpty(json)) {
					normalized = gson.fromJson(json, Boolean.class);
				}
			}
			String pointsTable = p.get("pointsTable");
			String moveTable = p.get("moveTable");
			String stopTable = p.get("stopTable");
			return new GeolifeUniversityDatabaseReader(!dataset.getRaw(), stopTable, moveTable, pointsTable, normalized);
		}
		if(dataset.getName().equalsIgnoreCase("geolife2")) {
			String pointsTable = p.get("pointsTable");
			String moveTable = p.get("moveTable");
			String stopTable = p.get("stopTable");
			String mappingTable = p.get("mappingTable");
			return new Geolife2DatabaseReader(!dataset.getRaw(), stopTable, moveTable, mappingTable, pointsTable);
		}
		if(dataset.getName().equalsIgnoreCase("geolife_transportation_mode")) {
			return new GeolifeTransportationModeDataReader();
		}
		if(dataset.getName().equalsIgnoreCase("crawdad_ums")) {
			return new CRAWDADDatabaseReader();
		}
		if(dataset.getName().equalsIgnoreCase("crawdad")) {
			Gson gson = new Gson();
			String[] roads = gson.fromJson(p.get("roads"), String[].class);
			String[] directions = gson.fromJson(p.get("directions"), String[].class);
			String[] regions = gson.fromJson(p.get("regions"), String[].class);
			return new SanFranciscoCabDatabaseReader(!dataset.getRaw(), roads, directions, regions);
		}
		if(dataset.getName().equalsIgnoreCase("foursquare")) {
			return new FoursquareDataReader();
		}
		if(dataset.getName().equalsIgnoreCase("hasl")) {
			Boolean normalized = Boolean.FALSE;
			Boolean shifted = Boolean.FALSE;
			Boolean rightHand = Boolean.FALSE;
			Boolean leftHand = Boolean.FALSE;
			if(p != null) {
				Gson gson = new Gson();
				String json = p.get("shift-origin");
				if(!StringUtils.isEmpty(json)) {
					shifted = gson.fromJson(json, Boolean.class);
				}
				json = p.get("normalized");
				if(!StringUtils.isEmpty(json)) {
					normalized = gson.fromJson(json, Boolean.class);
				}
				json = p.get("left_hand");
				if(!StringUtils.isEmpty(json)) {
					leftHand = gson.fromJson(json, Boolean.class);
				}
				json = p.get("right_hand");
				if(!StringUtils.isEmpty(json)) {
					rightHand = gson.fromJson(json, Boolean.class);
				}
			}
			return new HASLDatabaseReader(dataset.getRaw(), shifted, normalized, leftHand, rightHand);
		}
		if(dataset.getName().equalsIgnoreCase("ais-brest")) {
			return new AISBrestDataReader();
		}
		if(dataset.getName().equalsIgnoreCase("new-york_bus")) {
			Gson gson = new Gson();
			String[] lines = !StringUtils.isEmpty(p.get("lines")) ? gson.fromJson(p.get("lines"), String[].class) : null;
			String strategy = !StringUtils.isEmpty(p.get("strategy")) ? p.get("strategy").toUpperCase() : StopMoveStrategy.SMoT.toString();
			return new NewYorkBusDataReader(!dataset.getRaw(), lines, StopMoveStrategy.valueOf(strategy));
		}
		if(dataset.getName().equalsIgnoreCase("hermoupolis")) {
			if(p != null) {
				String file = p.get("file");
				return new HermoupolisDataReader(!dataset.getRaw(), file);
			}
			return new HermoupolisDataReader(!dataset.getRaw());
		}
		if(dataset.getName().equalsIgnoreCase("animals")) {
			return new AnimalsSTARKEYDataReader();
		}
		return null;
	}

}
