package tb.service.serialize;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tb.dao.IPartnerDao;
import tb.domain.Device;
import tb.domain.Partner;
import tb.domain.order.AddressPoint;
import tb.domain.order.Order;
import tb.domain.order.Requirement;
import tb.service.BrandingService;
import tb.service.PartnerService;
import tb.service.exceptions.ParseOrderException;
import tb.utils.DatetimeUtils;

public class OrderJsonParser {
	private static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	public static Order Json2OrderWithAirport(JSONObject jsonObject, Device device, IPartnerDao partnerDao,
			PartnerService partnerService, BrandingService brandingService)
			throws ParseOrderException, IOException {

		Order order = json2OrderCommon(jsonObject, device, partnerDao, partnerService, brandingService);
		order.setNotlater(false);
		return order;
	}

	public static Order Json2OrderWithBookingDate(JSONObject jsonObject, Device device, IPartnerDao partnerDao,
			PartnerService partnerService, BrandingService brandingService)
			throws ParseOrderException, IOException {

		Order order = json2OrderCommon(jsonObject, device, partnerDao, partnerService, brandingService);

		int bookMins = jsonObject.getInt("bookmins");
		Date bookingDate = DatetimeUtils.localTimeToUtc(new Date());
		bookingDate = new Date(bookingDate.getTime() + (bookMins * ONE_MINUTE_IN_MILLIS));
		order.setBookingDate(bookingDate);

		String bookType = jsonObject.getString("booktype");
		if (!bookType.equals("notlater") && !bookType.equals("exact")) {
			throw new ParseOrderException("bookType bad.");
		}
		order.setNotlater(bookType.equals("notlater"));
		return order;
	}

	private static Order json2OrderCommon(JSONObject jsonObject, Device device, IPartnerDao partnerDao,
			PartnerService partnerService, BrandingService brandingService)
			throws ParseOrderException, IOException {
		Order order = new Order();

		String recipientPhone = device.getPhone() != null ? device.getPhone() : jsonObject.optString("recipientPhone");
		order.setPhone(recipientPhone);

		String carBasket = jsonObject.optString("carbasket");
		order.setCarBasket(carBasket.isEmpty() ? null : carBasket);

		SortedSet<AddressPoint> addressPoints = new TreeSet<AddressPoint>();
		try {
			JSONObject sourceJson = jsonObject.getJSONObject("source");
			addressPoints.add(OrderJsonParser.parsePoint(sourceJson, order, 0));
		} catch (JSONException ex) {
			throw new ParseOrderException("source bad. " + ex.toString());
		}

		JSONArray destinationsJson = jsonObject.optJSONArray("destinations");
		if (destinationsJson != null) {
			for (int i = 0; i < destinationsJson.length(); i++) {
				JSONObject destinationJson = destinationsJson.getJSONObject(i);
				int index = 0;

				try {
					index = destinationJson.getInt("index");
				} catch (JSONException ex) {
					throw new ParseOrderException("destination index bad. " + ex.toString());
				}

				addressPoints.add(OrderJsonParser.parsePoint(destinationJson, order, index));
			}
		}
		order.setDestinations(addressPoints);

		Set<Requirement> requirements = new HashSet<Requirement>();
		try {
			JSONArray requirementsJson = jsonObject.optJSONArray("requirements");

			if (requirementsJson != null) {
				Long partnerId = 0L;
				if (device.getTaxi() != null && !device.getTaxi().isEmpty()) {
					Partner majorPartner = brandingService.getMajorPartner(device.getTaxi());
					if (majorPartner != null) {
						partnerId = majorPartner.getId();
					}
				}
				for (int i = 0; i < requirementsJson.length(); i++) {
					String name = requirementsJson.getString(i);
					Requirement currentRequirement = new Requirement();

					currentRequirement.setNeedCarCheck(partnerService.isNeedCarCheck(partnerId, name));
					currentRequirement.setType(name);
					currentRequirement.setOptions("yes");
					currentRequirement.setOrder(order);
					requirements.add(currentRequirement);
				}
			}
		} catch (JSONException ex) {
			throw new ParseOrderException("requirements bad. " + ex.toString());
		}
		order.setRequirements(requirements);
		order.setCarClass(jsonObject.getString("vehicleClass"));

		Set<Partner> offerPartners = new HashSet<Partner>();
		if (!jsonObject.isNull("partners")) {
			JSONArray partnersUuids = jsonObject.getJSONArray("partners");
			for (int i = 0; i < partnersUuids.length(); i++) {
				String currentPartnerUuid = partnersUuids.getString(i);
				Partner partner = partnerDao.get(currentPartnerUuid);
				offerPartners.add(partner);
			}
		}
		order.setOfferPartners(offerPartners);
		order.setComments(jsonObject.optString("comments", null));

		return order;
	}

	private static AddressPoint parsePoint(JSONObject pointJson, Order order, int index) {
		AddressPoint result = new AddressPoint();
		result.setOrder(order);
		result.setLon(pointJson.optDouble("lon", 0.0));
		result.setLat(pointJson.optDouble("lat", 0.0));
		result.setFullAddress(pointJson.optString("fullAddress", ""));
		result.setShortAddress(pointJson.optString("shortAddress", ""));
		result.setClosesStation(pointJson.optString("closestStation", ""));
		result.setCounty(pointJson.optString("country", ""));
		result.setLocality(pointJson.optString("locality", ""));
		result.setStreet(pointJson.optString("street", ""));
		result.setHousing(pointJson.optString("housing", ""));
		result.setIndexNumber(index);
		result.setEntrance(pointJson.optString("entrance", ""));
		result.setDescription(pointJson.optString("description", ""));

		return result;
	}
}
