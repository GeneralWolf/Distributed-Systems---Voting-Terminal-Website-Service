<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Election Details</title>
<script type="text/javascript">
	function onEditClick(elementId) {
		document.getElementById(elementId).removeAttribute('readonly');
	}
</script>
</head>
<body>
	<jsp:include page="header.jsp"/>
	
	<h2 style="text-align:center"> Election Details</h2>
	<div id="errorMsg">${errorMsg}</div>
	
	<div style="width:600px;float:left">
		<form name="editElectionForm" action="processEditElection" method="post">
		<table style="border-spacing:0 15px;"> 
			<tr> 
				<td> <label>Title :</label></td>
				<td> <input id="title" name="title" type="text" size="30" value="${election.title}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">						<!--basicamente verificação se o user é admin e se a eleição ainda não começou-->
					<input id="btnEdit1" type="button" value="Edit" onclick="onEditClick('title');"/>		<!--faz com que já não seja read only através do metodo onEditClick acima-->
					<input id="btnSave1" name="editTitle" type="submit" value="Save" />
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Description :</label></td>
				<td> <input id="description" name="description" type="text" size="30" value="${election.description}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit2" type="button" value="Edit" onclick="onEditClick('description');"/>
					<input id="btnSave2" name="editDescription" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Start Date :</label></td>
				<td> <input id="startDate" name="startDate" type="text" size="30" value="${election.startDate}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit3" type="button" value="Edit" onclick="onEditClick('startDate');"/>
					<input id="btnSave3" name="editStartDate" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>End Date :</label></td>
				<td> <input id="endDate" name="endDate" type="text" size="30" value="${election.endDate}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit4" type="button" value="Edit" onclick="onEditClick('endDate');"/>
					<input id="btnSave4" name="editEndDate" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Start Hour :</label></td>
				<td> <input id="startHour" name="startHour" type="text" size="30" value="${election.startHour}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit5" type="button" value="Edit" onclick="onEditClick('startHour');"/>
					<input id="btnSave5" name="editStartHour" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Start Minute :</label></td>
				<td> <input id="startMinute" name="startMinute" type="text" size="30" value="${election.startMinute}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit6" type="button" value="Edit" onclick="onEditClick('startMinute');"/>
					<input id="btnSave6" name="editStartMinute" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>End Hour :</label></td>
				<td> <input id="endHour" name="endHour" type="text" size="30" value="${election.endHour}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit7" type="button" value="Edit" onclick="onEditClick('endHour');"/>
					<input id="btnSave7" name="editEndHour" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>End Minute :</label></td>
				<td> <input id="endMinute" name="endMinute" type="text" size="30" value="${election.endMinute}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					 <input id="btnEdit8" type="button" value="Edit" onclick="onEditClick('endMinute');"/>
					<input id="btnSave8" name="editEndMinute" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Type :</label></td>
				<td> <input id="type" name="type" type="text" size="30" value="${election.type}" readonly />
				<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">
					<input id="btnEdit9" type="button" value="Edit" onclick="onEditClick('type');"/>
					<input id="btnSave9" name="editType" type="submit" value="Save"/>
				</c:if></td>
			</tr>
			<tr>
				<td> <label>Candidates :</label><br/><br/></td>
				<td><table>
				<thead>
					<tr><td><B>Name</B></td><td><B>Type</B></td></tr>
				</thead>
				<c:forEach items="${election.lists}" var="list" varStatus="loop">				<!--mostra todas as listas participantes numa eleição e o seu tipo-->
					<tr>
						<td> <input id="listname" name="name" type="text" size="30" readonly value="${list.name}" /></td>
						<td> <input id="listtype" name="type" type="text" size="20" readonly value="${list.type}" /></td>
						<c:if test="${election.status eq 'Done'}">
							<td> <label>${list.users.size()} &nbsp; votes </label></td>			<!--se a eleição já tiver terminado mostra os votos-->
						</c:if>
						<c:if test="${userRole eq 'Voter' && election.status eq 'Ongoing' && !isUserVoted}">		<!--se a elição ainda estiver a decorrer e o user for um voter consegue votar-->
							<td><input id="btnAddVote${loop.index}" type="button" value="Vote Now" onclick="location.href='AddVote?title=${election.title}&amp;name=${list.name}'"/></td>
						</c:if>
					</tr>
				</c:forEach></table></td>
			</tr>
			<c:if test="${userRole eq 'Voter' && election.status eq 'Ongoing' && !isUserVoted}">
				<tr>
					<td> </td>
					<td> <table><tr> 			<!--para voto branco e voto nulo-->
						<td><input id="btnWhiteVote" type="button" value="White Vote" onclick="location.href='AddVote?title=${election.title}&amp;name=White'"/></td>
						<td><input id="btnNullVote" type="button" value="Null Vote" onclick="location.href='AddVote?title=${election.title}&amp;name=Null'"/></td>
					</tr></table></td>
				</tr>
			</c:if>
		</table>
		</form>
		<c:if test="${userRole eq 'Admin' && election.status eq 'YetToStart'}">			<!--se um admin estiver na pagina e a eleiçao nao tiver começado este vai para a pagina que contem as mesas de voto da eleição-->
			<input id="btnVoteTable" type="button" value="Manage Voting Table" onclick="location.href='VotingTables?title=${election.title}'"/>
		</c:if>
	</div>
	<div style="width:300px;float:left;">
		<c:if test="${election.status eq 'Done'}">		<!--se a eleição ja tiver terminado mostra os resultados-->
			<br/><label><B>Result</B></label>
			<table style="border-spacing:0 15px;">
				<tr>
					<td> Won By <B>${election.wonBy} </B></td>
				</tr>
				<tr>
					<td> With <B>${election.percentWinVotes} % votes</B></td>
				</tr>
				<tr>
					<td> Of total <B>${election.totalVotes} votes</B></td>
				</tr>
				<tr>
					<td><input id="btnFBShare" type="button" value="Share Result In Facebook" onclick="location.href='ShareResultInFB?title=${election.title}'" /></td>
				</tr>
			</table>
		</c:if>
		<c:if test="${election.status eq 'Ongoing'}">			<!--se ainda estiver a decorrer e o user ja tiver votado, é mostrada mensagem com essa informação e é redirecionado para a pagina Elections-->
			<br/>
			<c:if test="${isUserVoted}">
				<label><B>You have voted!!</B></label><br/><br/>
			</c:if>
			<div id="currentlyVoted"></div> 
		</c:if> <br/>
		<input id="btnShowElections" type="button" value="Show Elections" 
				onclick="location.href='Elections'"/><br/><br/>
	</div>
</body>
</html>