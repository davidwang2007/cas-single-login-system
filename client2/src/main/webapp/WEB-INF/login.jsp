<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<html>
<body>
	<form action="<c:url value='/login'/>" method="POST" enctype="application/x-www-form-urlencoded">
		<p>
		<label>username:</label>
		<input type="text" name="username">
		</p>
		<p>
		<label>password:</label>
		<input type="password" name="password">
		</p>
		<input type="hidden" name="redirectUrl" value="${param.redirectUrl}">
		<p>${msg}</p>
		<button type="submit">Login</button>
	</form>
</body>
</html>
