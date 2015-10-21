package tb.maparea;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IMapAreaDao;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.maparea.MapArea;
import tb.utils.HttpUtils;

@Service
@EnableScheduling
public class MapareaSynch {
	private static final Logger log = LoggerFactory.getLogger(MapareaSynch.class);
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private MapareaBuilder mapareaBuilder;
	@Autowired
	private IMapAreaDao mapAreaDao;

	@Scheduled(cron = "0 10 * * * *")
	@Transactional
	public void synch() {
		List<Partner> partners = partnerDao.getPartnersNeedMapareaSynch();

		for (Partner partner : partners) {
			if (partner.getMapareaUrl() == null || partner.getMapareaUrl().isEmpty()) {
				continue;
			}

			try {
				InputStream inputStream = HttpUtils.makeGetRequest(partner.getMapareaUrl(), "application/xml");
				if (inputStream != null) {
					Date loadDate = new Date();
					List<MapArea> mapareas = mapareaBuilder.create(inputStream, partner, loadDate);
					updateMapareas(mapareas, partner, loadDate);
				}
			} catch (IOException e) {
				log.error("Maparea synch error: ", e);
			}
		}
	}

	private void updateMapareas(List<MapArea> mapareas, Partner partner, Date loadDate) {
		mapAreaDao.delete(partner);
		for (MapArea mapArea : mapareas) {
			mapAreaDao.add(mapArea);
		}
	}
}
