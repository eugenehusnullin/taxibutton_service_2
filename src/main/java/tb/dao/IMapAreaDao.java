package tb.dao;

import java.util.List;

import tb.domain.Partner;
import tb.domain.maparea.MapArea;

public interface IMapAreaDao {
	void add(MapArea mapArea);

	void delete(Partner partner);
	
	List<MapArea> getAll();

	void delete(Long id);

	MapArea get(Long id);
}
