<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>List of Elections</title>
<style>
      table,
      th,
      td {
        padding: 10px;
        border: 1px solid black;
        border-collapse: collapse;
        border-spacing:0 15px;
      }
    </style>
</head>
<body>
	<jsp:include page="header.jsp"/>
	 
	<h2 style="text-align:center"> Elections </h2>
	<div id="errorMsg">${errorMsg}</div> <br/>
	<c:if test="${userRole eq 'Admin'}">
		<div style="width:400px;float:left;">
			<input id="btnCreateElection" type="button" value="Create Election" 
					onclick="location.href='CreateElection'"/>
		</div>
		<div style="width:400px;float:left;">
			<input id="btnShowAdmin" type="button" value="Goto Admin" 
					onclick="location.href='AdminPage'"/>
		</div>
		<br/><br/>
	</c:if>
	<table> 
		<thead>
			<tr>
				<td><label><B>Title</B></label></td>
				<td><label><B>Description</B></label></td>
				<td><label><B>Type</B></label></td>
				<td><label><B>Start</B></label></td>
				<td><label><B>End</B></label></td>
				<td><label><B>Status</B></label></td>
				<td><label><B>Won By</B></label></td>
				<td><label><B>Percentage Votes</B></label>
			</tr>
		</thead>
		<c:forEach items="${allElections}" var="election">
		<tr> 
			<td> <label><a href="ElectionDetails?title=${election.title}"> ${election.title} </a></label></td>
			<td> <label>${election.description}</label> </td>
			<td> <label>${election.type}</label> </td>
			<td> <label>${election.startDate} &nbsp; ${election.startHour}:${election.startMinute}</label> </td>
			<td> <label>${election.endDate} &nbsp; ${election.endHour}:${election.endMinute}</label> </td>
			<td> <label>${election.status}</label> </td>
			<td> <label>${election.wonBy}</label> </td>
			<td> <label>${election.percentWinVotes}</label> </td>
		</tr>
		</c:forEach>
	</table>
</body>
</html>