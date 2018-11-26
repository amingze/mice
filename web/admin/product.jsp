<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>商品管理</title>
</head>
<body>
<p>----搜索----</p>
<form action="${contextPath}/admin_product_serch" method="get"  >
	商品名称:<input type="text" name="productName" >
	<input type="submit" value="搜索">
	${status}
</form>
<br>
<p>----添加----</p>
<form action="${contextPath}/admin_product_add" method="post" enctype='multipart/form-data' >
	商品名称:<input type="text" name="name">
	商品价格:<input type="number" name="price">
	<input id="fileId1" type="file" accept="image/png,image/gif" name="file"  />
	<input type="submit" value="添加">
	${status}
</form>
<br>
<p>----修改----</p>
<form action="${contextPath}/admin_product_updata" method="post">

<c:forEach items="${productlist}" var="product" varStatus="st">
	<input type="radio" name="id" value="${product.id }">
	<img src="${contextPath}/admin_product_getimg?id=${product.id }.jpg">
	<a href="product/id=${product.id}">${product.name}</a>
	<a href="${contextPath}/admin_product_delete?id=${product.id} ">删除</a>
	<p>${product.price}</p>
</c:forEach>

	<input type="text" name="name">
	<input type="number" name="price">
	<input type="submit" value="修改">
	${status}
</form>

</body>
</html>