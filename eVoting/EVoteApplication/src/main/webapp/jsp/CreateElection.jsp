<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Create New Election</title>

</head>
<body>
	<jsp:include page="header.jsp"/>
	
	<h2 style="text-align:center"> Create Election </h2>
	<div id="errorMsg">${errorMsg}</div>
	<form name="createElectionForm" action="processCreateElection" method="post">
		<table style="border-spacing:0 15px;"> 
			<tr> 
				<td> <label>Title :</label></td>
				<td> <input id="title" name="title" type="text" size="30"/> </td>
			</tr>
			<tr>
				<td> <label>Description :</label></td>
				<td> <input id="description" name="description" type="text" size="30"/></td>
			</tr>
			<tr>
				<td> <label>Start Date :</label></td>
				<td> <input id="startDate" name="startDate" type="text" size="30" placeholder="dd/MM/yyyy"/></td>
			</tr>
			<tr>
				<td> <label>End Date :</label></td>
				<td> <input id="endDate" name="endDate" type="text" size="30" placeholder="dd/MM/yyyy"/></td>
			</tr>
			<tr>
				<td> <label>Start Hour :</label></td>
				<td> <input id="startHour" name="startHour" type="text" size="30" placeholder="24 hr format"/></td>
			</tr>
			<tr>
				<td> <label>Start Minute :</label></td>
				<td> <input id="startMinute" name="startMinute" type="text" size="30" /></td>
			</tr>
			<tr>
				<td> <label>End Hour :</label></td>
				<td> <input id="endHour" name="endHour" type="text" size="30" placeholder="24 hr format"/></td>
			</tr>
			<tr>
				<td> <label>End Minute :</label></td>
				<td> <input id="endMinute" name="endMinute" type="text" size="30" /></td>
			</tr>
			<tr>
				<td> <label>Type :</label></td>
				<td> <input id="type" name="type" type="text" size="30" /></td>
			</tr>
			<tr>
				<td> <label>Candidates :</label><br/><br/></td>
				<td><textarea id="candidateList" name="candidateList" rows="10" cols="30" placeholder="Example: XXX/Student"></textarea><br/>
					<label>Enter candidate name and type in the format 'name/type' <br/> and press 'Enter' for next candidate.</label></td>
			</tr>
		</table>
		<input id="btnCreateElection" name="CreateElection" type="submit" value="Create Election"/>
		<input id="btnShowElections" type="button" value="Show Elections" 
				onclick="location.href='Elections'"/><br/><br/>
	</form>
	
</body>
</html>