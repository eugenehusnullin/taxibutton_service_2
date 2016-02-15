<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


	<form method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<br/>name <input type="text" name="name" value="Введите наименование партнера" />
		<br/>clid <input type="text" name="apiId" value="уникальный номер"/>
		<br/>apikey <input type="text" name="apiKey" value="уникальный номер"/>
		<br/>url <input type="text" name="apiurl" value="http://name.com:port" />
		<br/>TimeZone ID <input type="text" name="timezoneId" value="UTC"/>
		<br/>Опции машин брать из диспетчерской <input type="checkbox" name="customCarOptions"/>
		<br/>codeName <input type="text" name="codeName" value="" />
		
		<br />
		<br /> Map areas:
		<br />
		<c:forEach items="${mapareas}" var="maparea">
			<input type="checkbox" name="mapareas" value="${maparea.getId()}" />${maparea.getName()}<br />
		</c:forEach>
		
		<br /> <input type="submit" value="save" />
		
		<br/>
		<br/>
		<a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones" target="_blank">List of TimeZones, copy column TZ (for example: Europe/Moscow) </a>
		<br/>
		<br/>
		
	</form>