package tb.tariff;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.ITariffDao;
import tb.domain.Partner;
import tb.domain.Tariff;
import tb.domain.TariffType;
import tb.utils.HttpUtils;

@Service
public class TariffSynchProcessing {
	private static final Logger logger = LoggerFactory.getLogger(TariffSynch.class);

	@Autowired
	private TariffBuilder tariffBuilder;

	@Autowired
	private ITariffDao tariffDao;

	@Transactional
	public void makeTariffSynch(Partner partner) {
		logger.info("partner - " + partner.getName() + "(" + partner.getApiId() + ")");
		if (partner.getTariffUrl() == null || partner.getTariffUrl().isEmpty()) {
			logger.warn("tariff url is empty.");
			return;
		}

		try {
			InputStream inputStream = HttpUtils.makeGetRequest(partner.getTariffUrl(),
					partner.getTariffType() == TariffType.XML ? "application/xml" : "application/json");
			if (inputStream != null) {
				Date loadDate = new Date();
				List<Tariff> tariffs;

				if (partner.getTariffType() == TariffType.XML) {
					tariffs = tariffBuilder.createTariffsFromXml(inputStream, partner, loadDate);
				} else {
					tariffs = tariffBuilder.createTariffsFromJson(inputStream, partner, loadDate);
				}
				logger.info(tariffs.size() + " tariffs - pulled from partner.");
				updateTariffs(tariffs, partner, loadDate);
				logger.info("Tariffs saved to db.");
			}
		} catch (Exception e) {
			logger.error("Tariff synch error: ", e);
		}
	}

	private void updateTariffs(List<Tariff> tariffs, Partner partner, Date loadDate) {
		tariffDao.delete(partner);
		for (Tariff tariff : tariffs) {
			tariffDao.saveOrUpdate(tariff);
		}
	}

}
