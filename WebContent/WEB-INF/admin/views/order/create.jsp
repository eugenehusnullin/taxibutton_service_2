<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


	<form method="POST">
		<h3>Device</h3>
		Api id<br />
		<input type="text" name="apiId" value="815777e3-87e9-47e2-bd82-f70624b74517">
		<br />
		<h3>Order</h3>
		
		<br />
		Client phone:<br />
		<input type="text" name="phone" value="00000000000"/><br />
		<h3>Source</h3>
		Full address:
		<br />
		<input type="text" name="sFullAddress" value="Москва, Абельмановская Застава, площадь, , 8, корп: 1" />
		<br />
		Short address:
		<br />
		<input type="text" name="sShortAddress" value="Абельмановская Застава, площадь, , 8" />
		<br />
		Closest station:
		<br />
		<input type="text" name="sClosestStation" />
		<br />
		Point longitude:
		<br />
		<input type="text" name="sourceLon" value="37.673979"/>
		<br />
		Point latitude:
		<br />
		<input type="text" name="sourceLat" value="55.739039" />
		<br />
		Country:
		<br />
		<input type="text" name="sCountry" value="Россия"/>
		<br />
		Locality:
		<br />
		<input type="text" name="sLocality" value="Москва" />
		<br />
		Street:
		<br />
		<input type="text" name="sStreet" value="Абельмановская Застава, площадь"/>
		<br />
		Housing:
		<br />
		<input type="text" name="sHousing"  value="8"/>
		<br />
		<h3>Destination</h3>
		Full address:
		<br />
		<input type="text" name="dFullAddress" value="Москва, Авиаторов, улица, , 8" />
		<br />
		Short address:
		<br />
		<input type="text" name="dShortAddress" value="Авиаторов, улица, , 8"/>
		<br />
		Closest station:
		<br />
		<input type="text" name="dClosestStation" />
		<br />
		Point longitude:
		<br />
		<input type="text" name="destinationLon" value="37.678979"/>
		<br />
		Point latitude:
		<br />
		<input type="text" name="destinationLat" value="55.739039"/>
		<br />
		Country:
		<br />
		<input type="text" name="dCountry" value="Россия" />
		<br />
		Locality:
		<br />
		<input type="text" name="dLocality" value="Москва"/>
		<br />
		Street:
		<br />
		<input type="text" name="dStreet" value="Авиаторов, улица" />
		<br />
		Housing:
		<br />
		<input type="text" name="dHousing" value="8"/>
		<br />
		<h3>Bookmins</h3>
		<input type="text" name="bookmins" value="${bookmins}">
		<br />
		Booktype:<br />
		<input type="radio" name="booktype" value="notlater" checked /> notlater
		<input type="radio" name="booktype" value="exact" /> exact
		<br />
		<h3>Requirements</h3>
		<input type="checkbox" name="requirements" value="isAnimalTransport"> animals<br />
		<input type="checkbox" name="requirements" value="isCheck"> check<br />
		<input type="checkbox" name="requirements" value="isChildChair"> child chair <input type="text" name="childAge"/><br />
		<input type="checkbox" name="requirements" value="isConditioner"> conditioner<br />
		<input type="checkbox" name="requirements" value="noSmoking"> no smoking<br />
		<input type="checkbox" name="requirements" value="isUniversal"> universal<br />
		<input type="checkbox" name="requirements" value="isCoupon"> coupon<br />
		<br /><br />
		<h3>Vehicle class</h3>
		<input type="radio" name="vehicleClass" value="0" checked>Эконом<br />
		<input type="radio" name="vehicleClass" value="1">Комфорт<br />
		<input type="radio" name="vehicleClass" value="2">Бизнес<br />
		<br />
		<c:forEach items="${partners}" var="partner">
			<input type="checkbox" name="partners" value="${partner.getUuid()}" />${partner.getName()}<br />
		</c:forEach>
		<input type="submit" value="save" />
	</form>