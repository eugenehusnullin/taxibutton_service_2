package tb.car;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tb.cost.PartnerApi;
import tb.cost.PartnersApiKeeper;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.PartnerSettings;

@Service
public class CarOptionsSynchProcessing {
	private static final Logger logger = LoggerFactory.getLogger(CarSynch.class);

	@Autowired
	private PartnersApiKeeper partnersApiKeeper;
	@Autowired
	private IPartnerDao partnerDao;

	@Transactional
	public void makeCarOptionsSynch(Partner partner) {
		logger.info("Caroptions synch for partner - " + partner.getName() + "(" + partner.getApiId() + ")");
		PartnerApi api = partnersApiKeeper.getPartnerApi(partner);
		Call<String> call = api.settings();

		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				String responseString = response.body();
				PartnerSettings settings = new PartnerSettings();
				settings.setPartner(partner);
				settings.setSettings(responseString);

				partnerDao.saveSettings(settings);
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				logger.error(
						"Caroptions synch error for partner - " + partner.getName() + "(" + partner.getApiId() + ")"
								+ ": ",
						t);
			}
		});
	}
}
