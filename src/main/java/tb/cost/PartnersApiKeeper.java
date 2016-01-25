package tb.cost;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
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

					((OkHttpClient) retrofit.callFactory()).dispatcher().setMaxRequests(1000);
					((OkHttpClient) retrofit.callFactory()).dispatcher().setMaxRequestsPerHost(50);
					// int readTimeOut = ((OkHttpClient)retrofit.callFactory()).readTimeoutMillis();

					api = retrofit.create(PartnerApi.class);
					partners.put(partner.getId(), api);
				}
			}
		}
		return api;
	}
}
