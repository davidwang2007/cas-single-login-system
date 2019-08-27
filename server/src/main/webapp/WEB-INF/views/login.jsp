<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<body>
	<form action="<c:url value='/login'/>" method="POST" enctype="application/x-www-form-urlencoded">
		<p>
		<label>username:</label>
		<input type="text" name="username" value="${param.username}">
		</p>
		<p>
		<label>password:</label>
		<input type="password" name="password" value="">
		</p>
		<input type="hidden" name="redirectUrl" value="${param.redirectUrl}">
		<c:if test="${kick != null}">
			<p>
				<font color="red">用户已在另一台电脑[${ip}]登录,是否踢掉?</font><br>
				<label><input type="checkbox" name="kick" value="yes">踢</label>
			</p>
		</c:if>
		<c:if test="${msg != null}">
			<p><font color="red">${msg}</font></p>
		</c:if>
		<button type="submit">Login</button>
	</form>
</body>
</html>
