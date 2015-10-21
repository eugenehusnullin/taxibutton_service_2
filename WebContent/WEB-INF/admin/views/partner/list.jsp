<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<table border="1">
	<thead>
		<tr>
			<th>#</th>
			<th>Api id</th>
			<th>Api key</th>
			<th>Name</th>
			<th>Api url</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${partners}" var="partner">
			<tr class="infoTr">
				<td>${partner.getId()}</td>
				<td>${partner.getApiId()}</td>
				<td>${partner.getApiKey()}</td>
				<td>${partner.getName()}</td>
				<td>${partner.getApiurl()}</td>
			</tr>
			<tr class="actionTr">
				<td class="actionTd" colspan="5">
					<a href="../tariff/tariff?id=${partner.getId()}">Tariff</a>
					---
					<a href="../phone/blackList?id=${partner.getId()}">Black list</a>
					---
					<a href="qiwi?id=${partner.getId()}">QIWI</a>
					---
					<a href="delete?id=${partner.getId()}">Delete</a>
					---
					<a href="edit?id=${partner.getId()}">Edit</a>
					---
					<a href="cars?id=${partner.getId()}">Cars</a>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<br />
<a href="create">Create</a>
<br />
<a href="carsynch">Car synch (pull cars from dispatchers)</a>
<br />
<a href="tariffsynch">Tariff synch (pull tariffs from dispatchers)</a>
<br />
<br />
<br />