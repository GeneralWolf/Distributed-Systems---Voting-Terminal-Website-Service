<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Login to EVoting System</title>
<script>
  window.fbAsyncInit = function() {		//comentários relevantes a este código no header.jsp
    FB.init({
      appId      : '375726570423903',
      cookie     : true,
      xfbml      : true,
      version    : 'v10.0'
    });
      
    FB.AppEvents.logPageView();   
      
  };

  (function(d, s, id){
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "https://connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));
  
  function checkLoginState() {
	  FB.getLoginStatus(function(response) {
		  console.log('response : '+response);
		  var username;
		  var oauthUserInfo = new Object();
		  if (response.status === 'connected') {
		    // The user is logged in and has authenticated your
		    // app, and response.authResponse supplies
		    // the user's ID, a valid access token, a signed
		    // request, and the time the access token 
		    // and signed request each expire.
		    var uid = response.authResponse.userID;
		    oauthUserInfo['uid'] = uid;
		    var accessToken = response.authResponse.accessToken;
		    oauthUserInfo['accessToken'] = accessToken;
		    console.log("uid : "+uid+" *** accessToken : "+accessToken);
		    FB.api('/me', function(response1) {
		    	console.log(response1);
		        console.log('Good to see you, ' + response1.name + '.');
		        username = response1.name;
		        oauthUserInfo['username'] = username;
		     });
		    window.top.location.href = "http://localhost:8080/EVoteApplication/jsp/FacebookLogin?uid="+uid;
		  }
	  });
	}
  /*
//Fetch the user profile data from facebook
  function getFbUserData(){
      FB.api('/me', {locale: 'en_US', fields: 'id,first_name,last_name,email,link,gender,locale,picture'},
      function (response) {
          document.getElementById('fbLink').setAttribute("onclick","fbLogout()");
          document.getElementById('fbLink').innerHTML = 'Logout from Facebook';
          document.getElementById('status').innerHTML = '<p>Thanks for logging in, ' + response.first_name + '!</p>';
          document.getElementById('userData').innerHTML = '<h2>Facebook Profile Details</h2><p><img src="'+response.picture.data.url+'"/></p><p><b>FB ID:</b> '+response.id+'</p><p><b>Name:</b> '+response.first_name+' '+response.last_name+'</p><p><b>Email:</b> '+response.email+'</p><p><b>Gender:</b> '+response.gender+'</p><p><b>FB Profile:</b> <a target="_blank" href="'+response.link+'">click to view profile</a></p>';
      });
  }

  // Logout from facebook
  function fbLogout() {
      FB.logout(function() {
          document.getElementById('fbLink').setAttribute("onclick","fbLogin()");
          document.getElementById('fbLink').innerHTML = '<img src="images/fb-login-btn.png"/>';
          document.getElementById('userData').innerHTML = '';
          document.getElementById('status').innerHTML = '<p>You have successfully logout from Facebook.</p>';
      });
  }*/
</script>
</head>
<body>
	<h2 style="text-align:center"> Login to EVoting System</h2>
	<div id="errorMsg">${errorMsg}</div>
	<form name="loginForm" action="processLogin" method="post">
		<table style="border-spacing:0 15px;"> 
			<tr> 
				<td> <label>Username :</label></td>
				<td> <input id="name" name="name" type="text" size="30"/> </td>
			</tr>
			<tr>
				<td> <label>Password :</label></td>
				<td> <input id="password" name="password" type="password" size="30"/></td>
			</tr>
			<tr>
				<td> </td>
				<td> <input type="checkbox" name="role" /> Login As Admin </td>
			</tr>
			<tr>
				<td> <input style="float:right;" id="btnLogin" name="Login" type="submit" value="Log In"/> </td>
				<td> <label style="float:right;"> New User <a href="Register"><b> Register</b> </a>here!</label></td>
			</tr>
		</table>
	</form>
	<hr style="width:25%;text-align:left;margin-left:0"> <br/>
	
	 <fb:login-button scope="public_profile,email"  onlogin="checkLoginState();">		<!--codigo para o botao de login do facebook-->
	</fb:login-button>
</body>
</html>