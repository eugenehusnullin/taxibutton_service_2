package tb.cost;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import retrofit2.Retrofit;
import tb.domain.Partner;

@Service
public class PartnersApiKeeper {
	private Map<Long, PartnerApi> partners = new HashMap<>();
	private Object sync = new Object();

	public PartnerApi getPartnerApi(Partner partner) {

		PartnerApi api = partners.get(partner.getId());
		if (api == null) {
			synchronized (sync) {
				api = partners.get(partner.getId());
				if (api == null) {
					Retrofit retrofit = new Retrofit.Builder()
							.baseUrl(partner.getApiurl())
							.addConverterFactory(ScalarsConverterFactory.create())
							.build();

					api = retrofit.create(PartnerApi.class);
					partners.put(partner.getId(), api);
				}
			}
		}
		return api;
	}
}
