<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<table border="1">
	<thead>
		<tr>
			<th>Name</th>
			<th>Partners</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${brands}" var="brand">
			<tr>
				<td><a href="edit?id=${brand.getId()}">${brand.getName()}</a></td>
				<td>${brand.getPartners()}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<br />
<a href="edit">Create</a>
<br />
<br />
<br />