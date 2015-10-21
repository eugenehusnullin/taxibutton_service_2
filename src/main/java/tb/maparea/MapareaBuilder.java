package tb.maparea;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

import tb.domain.Partner;
import tb.domain.maparea.MapArea;
import tb.domain.maparea.Point;
import tb.domain.maparea.Polygon;

@Service
public class MapareaBuilder {

	public List<MapArea> create(InputStream inputStream, Partner partner, Date loadDate) throws IOException {
		String mapareaString = IOUtils.toString(inputStream, "utf8");
		JSONObject data = (JSONObject) new JSONTokener(mapareaString).nextValue();

		List<MapArea> mapAreas = new ArrayList<MapArea>();
		for (String key : data.keySet()) {
			JSONObject mapareaObject = data.getJSONObject(key);
			MapArea mapArea = create(key, mapareaObject, partner);
			mapAreas.add(mapArea);
		}

		return mapAreas;
	}

	private MapArea create(String name, JSONObject mapareaObject, Partner partner) {
		Polygon polygon = new Polygon();
		polygon.setAbout(name);
		polygon.setName(name);
		Set<Partner> partners = new HashSet<Partner>();
		partners.add(partner);
		polygon.setPartners(partners);
		List<Point> points = new ArrayList<>();
		polygon.setPoints(points);

		int revers = mapareaObject.optInt("revers", 0);
		JSONArray polygons = mapareaObject.getJSONArray("coordinates");
		JSONArray firstPolygon = polygons.getJSONArray(0);
		for (int i = 0; i < firstPolygon.length(); i++) {
			JSONArray couple = firstPolygon.getJSONArray(i);
			double lat = couple.getDouble(revers == 0 ? 0 : 1);
			double lon = couple.getDouble(revers == 0 ? 1 : 0);

			Point point = new Point();
			point.setLatitude(lat);
			point.setLongitude(lon);
			points.add(point);
		}

		return polygon;
	}
}
