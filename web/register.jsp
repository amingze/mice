<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>

<html>
<head>
<meta charset="UTF-8">
<title>用户注册</title>
</head>
<body>
<form action="/register" method="post" enctype="application/x-www-form-urlencoded">
	账号:<input type="text" name="name"><br>
	密码:<input type="password" name="passwd"><br>
	<input type="submit"  value ="注册">
	<p>${status}</p>
</form>
<a href="admin_user">管理</a>
</body>
</html>