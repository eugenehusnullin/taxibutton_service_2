<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

	<form method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<input type="hidden" name="partnerId" value="${partnerId}">
		<br /> name <input type="text" name="name" value="${name}" />
		<br /> clid<input type="text" name="apiId" value="${apiId}" />
		<br /> apikey <input type="text" name="apiKey" value="${apiKey}" />
		<br /> url <input type="text" name="apiUrl" value="${apiUrl}" />
		<br/>TimeZone ID <input type="text" name="timezoneId" value="${timezoneId}"/>
		<br />коментарий <input type="text" name="comment" value="${comment}" />
		
		<br />
		<br /> Map areas:
		<br />
		<c:forEach items="${mapareas}" var="maparea">
			<input type="checkbox" name="mapareas" value="${maparea.getId()}"
				<c:if test="${mapareasids.contains(maparea.getId())}">
					checked="checked"
				</c:if>
				/>${maparea.getName()}<br />
		</c:forEach>

		<br /><input type="submit" value="Save" />
	</form>