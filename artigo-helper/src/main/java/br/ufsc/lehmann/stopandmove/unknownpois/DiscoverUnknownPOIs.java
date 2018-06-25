package br.ufsc.lehmann.stopandmove.unknownpois;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.utils.EuclideanDistanceFunction;
import smile.clustering.DBScan;
import smile.math.distance.Distance;

public class DiscoverUnknownPOIs {
	private static String YEAR_MONTH = "_com_auditoria";
	
	public Map<Stop, Object> dbscan(Stop[] data, SpatialDistanceFunction distance, int minPts, double radius) {
		DBScan<Stop> dbScan = new DBScan<Stop>(data, new Distance<Stop>() {

			@Override
			public double d(Stop x, Stop y) {
				return distance.distance(x.getCentroid(), y.getCentroid());
			}
		}, minPts, radius);
		return Arrays.asList(data).stream().collect(Collectors.toMap(Function.identity(), s -> "Unknown POI " + dbScan.predict(s)));
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		int ratio = 200; // distance in meters to find neighbors
		int stopMinutes = 30;
		int maxDist = 200; // distance in meters to merge stops
		int toleranceMinutes = 2;
		
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.amsterdan_stop", null, "geom");
		Connection conn = source.getRetriever().getConnection();
		
		try {
			prepareDatabase(ratio, stopMinutes, conn);

			Map<Stop, Object> dbscan = extractUnknownPOIs(ratio, stopMinutes, conn);
			Set<Stop> stops = dbscan.keySet();

			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(
					"update involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins"
							+ "\" " + "set \"global_unknown_POI\" = ?, \"unknown_POI\" = ? where id = ?");
			for (Stop s : stops) {
				StopData roi = (StopData) s.getRegion();
				ps.setString(1, roi.globalUnknownPOIName);
				ps.setString(2, roi.unknownPOIName);
				ps.setInt(3, s.getStopId());
				ps.execute();
			}
			conn.commit();
			stops.forEach(stop -> System.out.println(stop));
		} finally {
			conn.close();
		}
		
	}

	private static void prepareDatabase(int ratio, int stopMinutes, Connection conn) throws SQLException {
		try {
			conn.createStatement().execute("ALTER TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\" DROP COLUMN \"global_unknown_POI\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		conn.createStatement().execute("ALTER TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\" ADD COLUMN \"global_unknown_POI\" character varying(100);");
		try {
			conn.createStatement().execute("ALTER TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\" DROP COLUMN \"unknown_POI\";");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		conn.createStatement().execute("ALTER TABLE involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\" ADD COLUMN \"unknown_POI\" character varying(100);");
	}

	private static Map<Stop, Object> extractUnknownPOIs(int ratio, int stopMinutes, Connection conn)
			throws SQLException {
		ResultSet stopsData = conn.createStatement().executeQuery(
				"SELECT id, start_timestamp, end_timestamp, longitude, latitude, \"closest_PDV\", \"PDV_distance\", is_home, id_colaborador_unidade, \"closest_colab_PDV\", \"colab_PDV_distance\"\r\n" + 
				"	FROM involves.\"stops_FastCBSMoT" + YEAR_MONTH + "_" + ratio + "mts_" + stopMinutes + "_mins" + "\";");
		Map<Integer, Stop> stops = new HashMap<>();
		while (stopsData.next()) {
			int stopId = stopsData.getInt("id");
			String closest_PDV = stopsData.getString("closest_PDV");
			double PDV_distance = stopsData.getDouble("PDV_distance");
			String closest_colab_PDV = stopsData.getString("closest_colab_PDV");
			double colab_PDV_distance = stopsData.getDouble("colab_PDV_distance");
			boolean is_home = stopsData.getBoolean("is_home");
			int id_colaborador_unidade = stopsData.getInt("id_colaborador_unidade");
			Stop stop = stops.get(stopId);
			if (stop == null) {
				stop = new Stop(stopId, null, //
						stopsData.getTimestamp("start_timestamp").getTime(), //
						stopsData.getTimestamp("end_timestamp").getTime(), //
						null, //
						-1, //
						null, //
						-1, //
						new TPoint(stopsData.getDouble("latitude"), stopsData.getDouble("longitude")), //
						null, //
						null//
				);
				stop.setUser(id_colaborador_unidade);
				stop.setRegion(new StopData(stopId, closest_PDV, PDV_distance, closest_colab_PDV, colab_PDV_distance, is_home, id_colaborador_unidade));
				stops.put(stopId, stop);
			}
		}
		DiscoverUnknownPOIs discoverUnknownPOIs = new DiscoverUnknownPOIs();
		EuclideanDistanceFunction distance = new EuclideanDistanceFunction();
		
		Collection<Stop> globalValues = stops.values().stream().filter((Stop s) -> ((StopData) s.getRegion()).pDV_distance > ratio ).collect(Collectors.toList());
		Stop[] globalArray = globalValues.toArray(new Stop[globalValues.size()]);
		
		int minPts = 1;
		Map<Stop, Object> dbscan = discoverUnknownPOIs.dbscan(globalArray, distance, minPts, ratio);
		dbscan.forEach((key, value) -> ((StopData) key.getRegion()).globalUnknownPOIName = (String) value);

		Collection<Stop> individualValues = stops.values().stream().filter((Stop s) -> ((StopData) s.getRegion()).colab_PDV_distance > ratio ).collect(Collectors.toList());
		Map<Integer, List<Stop>> grouped = individualValues.stream().collect(Collectors.groupingBy(Stop::getUser, Collectors.toList()));
		for (Map.Entry<Integer, List<Stop>> s : grouped.entrySet()) {
			List<Stop> valueUser = s.getValue();
			Stop[] arrayUser = valueUser.toArray(new Stop[valueUser.size()]);
			Map<Stop, Object> dbscanUser = discoverUnknownPOIs.dbscan(arrayUser, distance, minPts, ratio);
			dbscanUser.forEach((key, value) -> ((StopData) key.getRegion()).unknownPOIName = (String) value);
		}
		return dbscan;
	}
	
	private static class StopData {

		@Override
		public String toString() {
			return "StopData [stopId=" + stopId + ", closest_PDV=" + closest_PDV + ", pDV_distance=" + pDV_distance
					+ ", closest_colab_PDV=" + closest_colab_PDV + ", colab_PDV_distance=" + colab_PDV_distance
					+ ", is_home=" + is_home + ", id_colaborador_unidade=" + id_colaborador_unidade
					+ ", globalUnknownPOIName=" + globalUnknownPOIName + ", unknownPOIName=" + unknownPOIName + "]";
		}

		private int stopId;
		private String closest_PDV;
		private double pDV_distance;
		private String closest_colab_PDV;
		private double colab_PDV_distance;
		private boolean is_home;
		private int id_colaborador_unidade;
		private String globalUnknownPOIName;
		private String unknownPOIName;

		public StopData(int stopId, String closest_PDV, double pDV_distance, String closest_colab_PDV,
				double colab_PDV_distance, boolean is_home, int id_colaborador_unidade) {
					this.stopId = stopId;
					this.closest_PDV = closest_PDV;
					this.pDV_distance = pDV_distance;
					this.closest_colab_PDV = closest_colab_PDV;
					this.colab_PDV_distance = colab_PDV_distance;
					this.is_home = is_home;
					this.id_colaborador_unidade = id_colaborador_unidade;
		}
	}

}
