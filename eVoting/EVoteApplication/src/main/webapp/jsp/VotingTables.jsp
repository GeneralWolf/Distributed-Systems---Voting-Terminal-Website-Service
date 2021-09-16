<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Voting Tables</title>
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
<script type="text/javascript">
	function showAddForm() {
		document.getElementById("addForm").style.display = "block";
		document.getElementById("lstVotTable").style.display = "none";
	}
</script>
</head>
<body>
	<jsp:include page="header.jsp"/>
	
	<h2 style="text-align:center"> Voting Tables </h2>
	<div id="errorMsg">${errorMsg}</div>
	<div id="lstVotTable">
		<table> 
			<thead>
				<tr>
					<td><label><B>Department</B></label></td>
					<td><label><B>State</B></label></td>
					<td><label><B>Address</B></label></td>
					<td><label><B>Voting Terminals</B></label></td>
					<td></td>
				</tr>
			</thead>
			<c:forEach items="${listVotingTables}" var="votingTable" varStatus="loop">		<!--vai mostrar na pagina os dados de cada mesa de voto-->
			<tr> 
				<td> <label>${votingTable.department} </label></td>
				<td> <label>${votingTable.state}</label> </td>
				<td> <label>${votingTable.address}</label> </td>
				<td><table>
					<thead><tr><td> ID </td><td> State </td></tr></thead>
					<c:forEach items="${votingTable.votingTerminals}" var="votingTerminal">		<!--vai mostrar na pagina os dados de cada terminal das mesas de voto-->
						<tr>
						<td> <label>${votingTerminal.ID}</label> </td>
						<td> <label> ${votingTerminal.state}</label></td>
						</tr>
					</c:forEach>
					</table>
				</td>															<!--Vai diferenciar entre as eleições que ja foram adicionadas ou nao a uma mesa de voto para a opção de adicionar/remover-->
				<td> <c:if test="${not votingTable.addedToElection}">
					<input id="btnAdd${loop.index}" type="button" value="Add" onclick="location.href='AddVotTableToElection?department=${votingTable.department}'"/></c:if>
					<c:if test="${votingTable.addedToElection}">
					<input id="btnRemove${loop.index}" type="button" value="Remove" onclick="location.href='RemoveVotingTable?department=${votingTable.department}'"/></c:if>
				</td>
			</tr>
			</c:forEach>
		</table><br/>
		<input id="btnAdd" type="button" value="Add New Voting Table" onclick="showAddForm();"/>
		<input id="btnShowElections" type="button" value="Show Elections" 
				onclick="location.href='Elections'"/><br/><br/>
	</div>
	<div id="addForm" style="display:none;">
		<br/><h4><label>Add Voting Table</label></h4>
		<form name="addVotingTableForm" action="AddVotingTable" method="post">	<!--manda mensagem post com ação addVotingTable que está no VotingTableController-->
		<table> 
			<tr> 
				<td> <label>Department :</label></td>
				<td> <input id="department" name="department" type="text" size="30" /> </td>
			</tr>
			<tr>
				<td> <label>Address :</label></td>
				<td> <input id="address" name="address" type="text" size="100" /></td>
			</tr>
		</table><br/>
		<input id="btnAddForm" name="addVotingTable" type="submit" value="Add" />		<!--submit associado ao botão-->
		</form>
	</div>
</body>
</html>