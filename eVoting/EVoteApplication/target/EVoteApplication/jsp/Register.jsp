<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Register with EVoting System</title>
</head>
<body>
	<h2 style="text-align:center"> Register</h2>
	<div id="errorMsg">${errorMsg}</div>
	<form name="registrationForm" action="processRegistration" method="post">
		<table style="border-spacing:0 15px;"> 
			<tr> 
				<td> <label>Username :</label></td>
				<td> <input id="name" name="name" type="text" size="30"/> </td>
			</tr>
			<tr> 
				<td> <label>Role :</label></td>
				<td> <input id="role" name="role" type="text" size="30" placeholder="Admin / Voter"/> </td>
			</tr>
			<tr>
				<td> <label>Type :</label></td>
				<td> <input id="type" name="type" type="text" size="30"/></td>
			</tr>
			<tr>
				<td> <label>Password :</label></td>
				<td> <input id="password" name="password" type="password" size="30"/></td>
			</tr>
			<tr>
				<td> <label>Department :</label></td>
				<td> <input id="department" name="department" type="text" size="30"/></td>
			</tr>
			<tr>
				<td> <label>Phone Number :</label></td>
				<td> <input id="phoneNumber" name="phoneNumber" type="number" size="30"/></td>
			</tr>
			<tr>
				<td> <label>Residence Address :</label></td>
				<td> <input id="residenceAddress" name="residenceAddress" type="text" size="30"/></td>
			</tr>
			<tr>
				<td> <label>CC Number :</label></td>
				<td> <input id="ccNumber" name="ccNumber" type="number" size="30"/></td>
			</tr>
			<tr>
				<td> <label>CC Expiration Date :</label></td>
				<td> <input id="ccExpiryDate" name="ccExpiryDate" type="text" size="30"/></td>
			</tr>
		</table><br/>
		
		<input id="btnRegister" name="Register" type="submit" value="Register"/>
	</form>
	
</body>
</html>