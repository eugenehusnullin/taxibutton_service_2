<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<table border="1">
	<thead>
		<tr>
			<th>disp</th>
			<th>uuid</th>
			<th>name</th>
			<th>state</th>
			<th>date</th>
			<th>lat</th>
			<th>lon</th>
			<th>requirmets</th>
			<th>carclass</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${cars}" var="car">
			<tr
				<c:if test="${car.isGeoObsolete()}">
					bordercolor="red"
				</c:if>
			>
				<td>${car.getDisp()}</td>
				<td>${car.getUuid()}</td>
				<td>${car.getName()}</td>
				<td>${car.getState()}</td>
				<td>${car.getDate()}</td>
				<td>${car.getLat()}</td>
				<td>${car.getLon()}</td>
				<td>${car.getRequirmets()}</td>
				<td>${car.getCarclass()}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<br />
<br />
<br />
