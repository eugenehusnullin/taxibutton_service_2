package tb.service.serialize;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tb.dao.IBrandDao;
import tb.dao.IPartnerDao;
import tb.domain.Brand;
import tb.domain.Device;
import tb.domain.Partner;
import tb.domain.order.AddressPoint;
import tb.domain.order.Order;
import tb.domain.order.Requirement;
import tb.service.PartnerService;
import tb.service.exceptions.ParseOrderException;
import tb.utils.DatetimeUtils;

public class OrderJsonParser {
	private static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	public static Order Json2Order(JSONObject jsonObject, Device device, IPartnerDao partnerDao,
			PartnerService partnerService, IBrandDao brandDao)
			throws ParseOrderException, IOException {

		Order order = new Order();

		String recipientPhone = device.getPhone() != null ? device.getPhone() : jsonObject.optString("recipientPhone");

		String carBasket = jsonObject.optString("carbasket");
		order.setCarBasket(carBasket.isEmpty() ? null : carBasket);

		int bookMins = jsonObject.getInt("bookmins");
		Date bookingDate = DatetimeUtils.localTimeToUtc(new Date());
		bookingDate = new Date(bookingDate.getTime() + (bookMins * ONE_MINUTE_IN_MILLIS));

		String bookType = jsonObject.getString("booktype");
		if (!bookType.equals("notlater") && !bookType.equals("exact")) {
			throw new ParseOrderException("bookType bad.");
		}
		order.setNotlater(bookType.equals("notlater"));

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

		Set<Requirement> requirements = new HashSet<Requirement>();
		try {
			JSONArray requirementsJson = jsonObject.optJSONArray("requirements");

			if (requirementsJson != null) {
				// for (int i = 0; i < requirementsJson.length(); i++) {
				// JSONObject requirementJson = requirementsJson.getJSONObject(i);
				// Requirement currentRequirement = new Requirement();
				//
				// try {
				// currentRequirement.setType(requirementJson.getString("name"));
				// currentRequirement.setOptions(requirementJson.getString("value"));
				// } catch (JSONException ex) {
				// throw new ParseOrderException("requirement bad. " + ex.toString());
				// }
				//
				// currentRequirement.setOrder(order);
				// requirements.add(currentRequirement);
				// }

				Long partnerId = 0L;
				if (device.getTaxi() != null && !device.getTaxi().isEmpty()) {
					Brand brand = brandDao.get(device.getTaxi());
					List<Partner> partners = brand.getServices().stream()
							.map(p -> p.getPartner()).collect(Collectors.toList());
					if (partners != null && partners.size() > 0) {
						partnerId = partners.get(0).getId();
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

		order.setComments(jsonObject.optString("comments", null));

		order.setPhone(recipientPhone);
		order.setBookingDate(bookingDate);
		order.setDestinations(addressPoints);
		order.setRequirements(requirements);
		order.setOfferPartners(offerPartners);

		return order;
	}

	private static AddressPoint parsePoint(JSONObject pointJson, Order order, int index) {
		AddressPoint result = new AddressPoint();
		Double lon = null;
		Double lat = null;
		String fullAddress = null;
		String shortAddress = null;
		String closestStation = null;
		String country = null;
		String locality = null;
		String street = null;
		String housing = null;
		String entrance = null;
		String description = null;

		lon = pointJson.optDouble("lon", 0.0);
		lat = pointJson.optDouble("lat", 0.0);
		fullAddress = pointJson.optString("fullAddress", "");
		shortAddress = pointJson.optString("shortAddress", "");
		closestStation = pointJson.optString("closestStation", "");
		country = pointJson.optString("country", "");
		locality = pointJson.optString("locality", "");
		street = pointJson.optString("street", "");
		housing = pointJson.optString("housing", "");
		entrance = pointJson.optString("entrance", "");
		description = pointJson.optString("description", "");

		result.setLon(lon);
		result.setLat(lat);
		result.setFullAddress(fullAddress);
		result.setShortAddress(shortAddress);
		result.setClosesStation(closestStation);
		result.setCounty(country);
		result.setLocality(locality);
		result.setStreet(street);
		result.setHousing(housing);
		result.setOrder(order);
		result.setIndexNumber(index);
		result.setEntrance(entrance);
		result.setDescription(description);

		return result;
	}
}
