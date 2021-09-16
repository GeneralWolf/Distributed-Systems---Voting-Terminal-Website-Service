<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Live status</title>
<script type="text/javascript">
	
</script>
</head>
<body>
	<jsp:include page="header.jsp"/>
	
	<h2 style="text-align:center"> Live Status</h2><br/>
	<div style="width:400px;float:left;">
		<label><B>Voting Terminals</B></label>
		<div id="liveVotingTable"></div>
	</div>
	<div style="width:400px;float:left;">
		<label><B>Connected Users</B></label>
		<div id="liveConnected"></div>
	</div>
	<div style="width:200px;float:left;">
		<input id="btnCreateAdmin" type="button" value="Create Admin" 
				onclick="location.href='Register'"/> <br/> <br/>
		<input id="btnShowElections" type="button" value="Show Elections" 
				onclick="location.href='Elections'"/><br/><br/>
	</div>
</body>
</html>