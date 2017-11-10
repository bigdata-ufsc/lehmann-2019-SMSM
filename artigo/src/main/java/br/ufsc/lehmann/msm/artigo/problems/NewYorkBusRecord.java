package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class NewYorkBusRecord {

	private int gid;
	private Timestamp time;
	private int vehicleId;
	private String route;
	private String tripId;
	private double longitude;
	private double latitude;
	private double distanceAlongTrip;
	private int direction;
	private String phase;
	private double nextStopDistance;
	private String nextStopId;
	private Integer semanticStop;
	private Integer semanticMoveId;
	private String POI;

	public NewYorkBusRecord(int gid, Timestamp time, int vehicleId, String route, String tripId, double longitude, double latitude, double distanceAlongTrip,
			int direction, String phase, double nextStopDistance, String nextStopId, String POI, Integer semanticStop, Integer semanticMoveId) {
		this.gid = gid;
		this.time = time;
		this.vehicleId = vehicleId;
		this.route = route;
		this.tripId = tripId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.distanceAlongTrip = distanceAlongTrip;
		this.direction = direction;
		this.phase = phase;
		this.nextStopDistance = nextStopDistance;
		this.nextStopId = nextStopId;
		this.semanticStop = semanticStop;
		this.semanticMoveId = semanticMoveId;
		this.POI = POI;
	}

	public NewYorkBusRecord(int gid, Timestamp time, int vehicleId, String route, String tripId, double longitude, double latitude, double distanceAlongTrip,
			int direction, String phase, double nextStopDistance, String nextStopId, Integer semanticStop, Integer semanticMoveId) {
		this(gid, time, vehicleId, route, tripId, longitude, latitude, distanceAlongTrip, direction, phase, nextStopDistance, nextStopId, null, semanticStop, semanticMoveId);
	}

	public int getGid() {
		return gid;
	}

	public Timestamp getTime() {
		return time;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public String getRoute() {
		return route;
	}

	public String getTripId() {
		return tripId;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getDistanceAlongTrip() {
		return distanceAlongTrip;
	}

	public int getDirection() {
		return direction;
	}

	public String getPhase() {
		return phase;
	}

	public double getNextStopDistance() {
		return nextStopDistance;
	}

	public String getNextStopId() {
		return nextStopId;
	}

	public Integer getSemanticStop() {
		return semanticStop;
	}

	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}

	public String getPOI() {
		return POI;
	}
}
