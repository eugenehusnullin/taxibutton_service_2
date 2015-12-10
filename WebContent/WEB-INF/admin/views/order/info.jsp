<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<table border="1">
	<thead>
		<tr>			
			<th>Date</th>
			<th>Info</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${infos}" var="info">
			<tr>
				<td>${info.getNoteDate()}</td>
				<td>${info.getNote()}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>