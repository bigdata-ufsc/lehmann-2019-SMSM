package br.ufsc.lehmann.geocode.reverse;

import de.westnordost.osmapi.map.data.LatLon;

public class TrafficLight {

	private long id;
	private LatLon position;

	public TrafficLight(long id, LatLon position) {
		this.id = id;
		this.position = position;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LatLon getPosition() {
		return position;
	}

	public void setPosition(LatLon position) {
		this.position = position;
	}

}
