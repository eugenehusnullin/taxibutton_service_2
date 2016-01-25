package tb.cost;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PartnerApi {
	@Headers({"Content-Type: application/json"})
	@POST("/1.x/cost")
	Call<String> cost(@Body String body);
}
