package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Date;
import java.sql.Timestamp;

public class DublinBusRecord {

	private Timestamp time;
	private int lineId;
	private String journey_pattern;
	private int vehicle_journey;
	private String operator;
	private boolean congestion;
	private double longitude;
	private double latitude;
	private int block_journey_id;
	private int vehicle_id;
	private int stop_id;
	private int gid;
	private Integer semanticStopId;

	public DublinBusRecord(int gid, Timestamp time, int lineId, String journey_pattern, int vehicle_journey, String operator, boolean congestion, double longitude, double latitude,
			int block_journey_id, int vehicle_id, int stop_id, Integer semanticStopId) {
				this.gid = gid;
				this.time = time;
				this.lineId = lineId;
				this.journey_pattern = journey_pattern;
				this.vehicle_journey = vehicle_journey;
				this.operator = operator;
				this.congestion = congestion;
				this.longitude = longitude;
				this.latitude = latitude;
				this.block_journey_id = block_journey_id;
				this.vehicle_id = vehicle_id;
				this.stop_id = stop_id;
				this.semanticStopId = semanticStopId;
	}

	public Timestamp getTime() {
		return time;
	}

	public int getLineId() {
		return lineId;
	}

	public String getJourney_pattern() {
		return journey_pattern;
	}

	public int getVehicle_journey() {
		return vehicle_journey;
	}

	public String getOperator() {
		return operator;
	}

	public boolean isCongestion() {
		return congestion;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public int getBlock_journey_id() {
		return block_journey_id;
	}

	public int getVehicle_id() {
		return vehicle_id;
	}

	public int getStop_id() {
		return stop_id;
	}

	public Integer getSemanticStopId() {
		return semanticStopId;
	}

	public int getGid() {
		return gid;
	}

}
