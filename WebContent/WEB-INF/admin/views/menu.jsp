<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="mainDiv">
	<a href="<c:url value="/admin/order/list"/>">1. Заказы</a> <br />
	<a href="<c:url value="/admin/order/create"/>">2. Создать заказ</a> <br />
	<a href="<c:url value="/admin/partner/list"/>">3. Диспетчерские</a>	<br />
	<a href="<c:url value="/admin/brand/list"/>">4. Бренды</a>	<br />
	<a href="<c:url value="/admin/device/list"/>">5. Устройства</a> <br />
	<a href="<c:url value="/admin/maparea/list"/>">6. Геозоны Кнопки</a> <br />
</div>