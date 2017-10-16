package br.ufsc.lehmann.geocode.reverse;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.handler.DefaultMapDataHandler;

public class TrafficLightsExtractor {
	private static final OsmConnection osm = new OsmConnection(
            "https://api.openstreetmap.org/api/0.6/",
            "my user agent", null);
	
	public List<TrafficLight> retrieveFrom(Stop s) {
		BoundingBox box = createBB((s.getStartPoint()), (s.getEndPoint()));
		MapDataDao map = new MapDataDao(osm);
		final List<TrafficLight> ret = new ArrayList<>();
		map.getMap(box, new DefaultMapDataHandler() {
			
			@Override
			public void handle(Node node) {
				if(node.getTags() != null && node.getTags().values().contains("traffic_signals")) {
					ret.add(new TrafficLight(node.getId(), node.getPosition()));
				}
			}
		});
		return ret;
	}

	private BoundingBox createBB(TPoint... points) {
		double minX = 180, minY = 180, maxX = -180, maxY = -180;
		for (TPoint p : points) {
			if(p.getX()< minX) {
				minX = p.getX();
			}
			if(p.getY() < minY) {
				minY = p.getY();
			}
			if(p.getX() > maxX) {
				maxX = p.getX();
			}
			if(p.getY()> maxY) {
				maxY = p.getY();
			}
				
		}
		return new BoundingBox(minX, minY, maxX, maxY);
	}

	public static Point pointFromLatLon(LatLon position) {
		return new Point(position.getLatitude(), position.getLongitude());
	}
}
