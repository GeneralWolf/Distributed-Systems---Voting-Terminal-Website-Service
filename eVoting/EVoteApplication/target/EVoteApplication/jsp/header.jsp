<script type="text/javascript">
/* Start of link facebook */
window.fbAsyncInit = function() {		//facebook API data
    FB.init({
      appId      : '473489910389407',
      cookie     : true,
      xfbml      : true,
      version    : 'v10.0'
    });
      
    FB.AppEvents.logPageView();   
      
  };

  (function(d, s, id){									//TODO check better
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "https://connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));

  /**
   * Função verifica se o utilizador esta logged in com o API do facebook //TODO???
   */
  function checkLoginState() {
	  FB.getLoginStatus(function(response) {			//recebe uma resposta como argumento
		  console.log('response : '+response.status);
		  var username;
		  var oauthUserInfo = new Object();
		  if (response.status === 'connected') {			//verifica se esta conectado usando a resposta
		    // The user is logged in and has authenticated your
		    // app, and response.authResponse supplies
		    // the user's ID, a valid access token, a signed
		    // request, and the time the access token 
		    // and signed request each expire.
		    var uid = response.authResponse.userID;			//guarda o id do user na var uid
		    oauthUserInfo['uid'] = uid;
		    var accessToken = response.authResponse.accessToken;	//busca o acessToken atraves da resposta e guarda esse token
		    oauthUserInfo['accessToken'] = accessToken;
		    console.log("uid : "+uid+" *** accessToken : "+accessToken);
		    FB.api('/me', function(response1) {
		    	console.log(response1);
		        console.log('Good to see you, ' + response1.name + '.');
		        username = response1.name;
		        oauthUserInfo['username'] = username;
		     });
		    location.href = "http://localhost:8080/EVoteApplication/jsp/LinkFacebook?uid="+uid;
		  }
	  });
	}
  	/* End of Facebook */
	/**
	 * Abre socket, guarda os dados enviados pelo RmiServer atraves desta, e tambem gere o close da socket
	 */
	var socket;
	function openSocket() {
		var votTableDiv, objVotTable, arrTerminals, objTerminal, divCurrentlyVoted, divCurrentlyConnected, arrJson;
		var innerHtml;
		
		// open the connection if one does not exist
	      if (socket !== undefined
	        && socket.readyState !== WebSocket.CLOSED) {
	        return;
	      }
		
		socket = new WebSocket('ws://localhost:8000');				//abre socket no endereço escolhido
		socket.onerror = function(error) {
		  console.log("[error] ${error.message}");
		};
		// Add an event listener for when a connection is open
		socket.onopen = function() {
		  console.log('WebSocket connection opened. Ready to send messages.');
		 
		  // Send a message to the server
		  //socket.send('Hello, from WebSocket client!');
		};
		 
		// Add an event listener for when a message is received from the server
		socket.onmessage = function(message) {
		  console.log('Message received from server: ' + message.data);
		 // if(typeof (message.data) === 'Object') {
			  var jsonObject = JSON.parse(message.data);					//da parse dos ficheiros JSON que contem os dados enviados pelo RmiServer e guarda-los
			  votTableDiv = document.getElementById("liveVotingTable");
			  if(votTableDiv) {
				  innerHtml = "<table> <thead> <tr> <td>Department </td> <td> <State> </td><td> Terminal ID </td> <td> State </td> </tr></thead>";
				  
				  var arrVotTable = jsonObject.VotingTables;
				  if(arrVotTable != null && arrVotTable.length > 0) {
					  for(var i = 0; i < arrVotTable.length; i++) {
						  objVotTable = arrVotTable[i];
						  arrTerminals = objVotTable.votingTerminals;				//por cada terminal de cada voting table
						  if(arrTerminals != null && arrTerminals.length > 0) {
							  for(var j = 0; j < arrTerminals.length; j++) {
								  objTerminal = arrTerminals[j];					//guarda os terminais e o seu estado de cada voting table/departamento
							  	innerHtml = innerHtml + "<tr><td>"+objVotTable.department+"</td><td>"+objVotTable.state+"</td><td>"+objTerminal.terminalId+"</td><td>"+objTerminal.terminalState+"</td></tr>";
							  }
						  } else {		//se nao houverem terminais ativos so guarda o departamento e estado da mesa de voto
							  innerHtml = innerHtml + "<tr><td>"+objVotTable.department+"</td><td>"+objVotTable.state+"</td><td></td><td></td></tr>";
						  }
					  }
				  } else {
					  innerHtml = innerHtml + "</table>";
				  }
				  votTableDiv.innerHTML = innerHtml;
			  }
			  
			  innerHtml = '';
			  divCurrentlyVoted = document.getElementById("currentlyVoted");	//busca e guarda eleições que ja tem votos e dados em relação ao seu numero de votos
			  if(divCurrentlyVoted) {
				  var noOfVotes = 0;
				  var title = document.getElementById("title").value;
				  arrJson = jsonObject.UsersVotedElections;
				  if(arrJson != null && arrJson.length > 0) {
					  for(var i = 0; i < arrJson.length; i++) {
						  tempJson = arrJson[i];
						  for (let x in tempJson) {
							 if(x === title)
								 noOfVotes = tempJson[x];
						  }
					  }
				  }
				  divCurrentlyVoted.innerHTML = noOfVotes + " Voted So far!";
			  }
			  
			  innerHtml = '';
			  divCurrentlyConnected = document.getElementById("liveConnected");
			  if(divCurrentlyConnected) {
				  innerHtml = "<h3>"+jsonObject.ConnectedUsers+"</h3>";
				  divCurrentlyConnected.innerHTML = innerHtml;
			  }
		  //}
		};														//abaixo é basicamente para fechar as sockets
		socket.onclose = function(event) {
		  console.log('Closing socket connection!!');
		};
	}
	function closeSocket() {
		if (socket && socket.readyState === WebSocket.OPEN) {
	      socket.close();
	   }
	}
	function doLogout() {
		closeSocket();
		location.href='Logout'
	}
	
	window.addEventListener("load", openSocket, false);
</script>
<div style="width:500px;float:right;">
	<label style="padding-right:30px;"> <B> Welcome ${username }! </B></label>
	<fb:login-button scope="public_profile,email"  onlogin="checkLoginState();">
	</fb:login-button>
	<input id="btnLogout" type="button" value="Log Out" onclick="doLogout();"/>
</div>