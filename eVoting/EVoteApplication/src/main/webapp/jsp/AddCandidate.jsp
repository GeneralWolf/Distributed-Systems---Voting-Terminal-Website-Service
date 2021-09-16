<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Add Candidate</title>
</head>
<body>
	<jsp:include page="header.jsp"/>
	
	<h2 style="text-align:center"> Add Candidate </h2>
	<div id="errorMsg">${errorMsg}</div>
	<form name="addCandidateForm" action="processAddCandidate" method="post">
		<table style="border-spacing:0 15px;"> 
			<tr> 
				<td> <label>Name :</label></td>
				<td> <input id="name" name="name" type="text" size="30"/> </td>
			</tr>
			<tr>
				<td> <label>Type :</label></td>
				<td> <input id="type" name="type" type="text" size="30"/></td>
			</tr>
		</table>
		<input id="btnAddCandidate" name="AddCandidate" type="submit" value="Add Candidate"/>
		<input id="btnShowElections" type="button" value="Show Elections" 
				onclick="location.href='Elections'"/><br/><br/>
	</form>
</body>
</html>