<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

	<form method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		
		<input type="hidden" name="brandId" value="${brand.getId()}">
		<br /> name <input type="text" name="name" value="${brand.getCodeName()}" />
				
		<br />
		<br /> Настройка партнеров:
		<br /> <i>(для исключения партнера установите приоритет равным нулю)</i>
		<br /> <i>(приоритет это число от 1 до 8, оно соответствует номеру цикла обработки, каждый цикл происходит через 20 сек. т.е. если приоритет равен 3 то заказ отправится партнеру через 60 секунд)</i>
		<br /> <i>(признак "Главный" устанавливается партнеру у которого будет запрашиваться предварительная сумма и опции)</i>
		<br />
		<c:forEach var="i" begin="0" end="${services.size() - 1}" >
			<input type="hidden" name="service_${i}_partnerid" value="${services.get(i).getPartnerId()}" />
			наименование: <b>${services.get(i).getPartnerName()}</b> | 
			приоритет: <input type="text" name="service_${i}_priority" value="${services.get(i).getPriority()}" /> |
			главный: <input type="radio" name="major" value="${services.get(i).getPartnerId()}"
				<c:if test="${services.get(i).isMajor()}">
					checked="checked"
				</c:if> 
			/>
			<br />
		</c:forEach>

		<br /><input type="submit" value="Save" />
	</form>