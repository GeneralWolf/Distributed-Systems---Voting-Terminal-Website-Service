package com.evote.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.evote.models.Election;
import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;
import com.evote.util.Utility;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

@Controller
@RequestMapping("/jsp")
public class FacebookController {
	@Autowired
	private RmiClient rmiClient;
	public static RmiServer rmiServer;

	@Value("473489910389407")
	private String appId;
	
	@Value("6847c61f58738a32005bf0e5f4d56aed")
	private String appSecret;
	
	@Value("http://localhost:8080/EVoteApplication/jsp/facebook-callback")
	private String callback;
	
	ScopeBuilder scopeBuilder = new ScopeBuilder("email","pages_read_engagement","pages_manage_posts","pages_manage_metadata");
	private static String longAccessToken = null;
	private static String pageAccessToken = null;
	
	@RequestMapping( value="/facebook", method = RequestMethod.GET)
	public void facebook(HttpServletRequest request, 
	        @RequestParam(value = "page", required = true) String page,
	        HttpServletResponse response) throws Exception {

	    try {
	    	OAuth20Service service =  new ServiceBuilder(appId)
	            .apiSecret(appSecret)
	            .callback(callback)
	            .defaultScope(scopeBuilder)
	        .build(FacebookApi.instance());

	        String authUrl = service.getAuthorizationUrl(); 

	        response.sendRedirect(authUrl);

	    } catch (Exception e) {

	        response.sendRedirect("/oauthFail");
	    }       

	}

	@RequestMapping( value="/facebook-callback", method = RequestMethod.GET)
	public ModelAndView facebookCallback(HttpServletRequest servletRequest,
	        @RequestParam(value = "code", required = false) String code,
	        @RequestParam(value = "error", required = false) String error,
	        HttpServletResponse servletResponse) throws Exception {
		String title = null;
		OAuthRequest request = null;
		com.github.scribejava.core.model.Response response = null;
		JSONObject jsonObj = null;
		JSONObject pageJson = null;
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArr = null;
	    try {	    	
	    	title = (String) servletRequest.getSession().getAttribute("selectedElection");
	    	System.out.println(" title inside facebook callback : "+title);
	    	
	        OAuth20Service service =  new ServiceBuilder(appId)
	            .apiSecret(appSecret)
	            .callback(callback)
	            .defaultScope(scopeBuilder)
	        .build(FacebookApi.instance());

	        OAuth2AccessToken accessToken = service.getAccessToken(code);  
	        request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v3.2/me");
	        service.signRequest(accessToken, request);        
	        response = service.execute(request);

	        String body = response.getBody();
	        System.out.println("response from fb : "+body);
//	        JSONObject jObject = new JSONObject(body);
//	        String email = jObject.getString("email");

	        //success. use the email to create an account or if the email address exists, direct a userto their account page
	        
	        //To get long lived access token
	        request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/oauth/access_token?grant_type=fb_exchange_token&client_id="+appId+"&client_secret="+appSecret+"&fb_exchange_token="+accessToken.getAccessToken());
	        response = service.execute(request);
	        System.out.println("response after exchange token : "+response.getBody());
	        jsonObj = (JSONObject) jsonParser.parse(response.getBody());
	        longAccessToken = (String) jsonObj.get("access_token");
	        
	        //To get page_access_token
	        request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me/accounts?access_token="+longAccessToken);
	        response = service.execute(request);
	        System.out.println("response after getting page access token : "+response.getBody());
	        jsonObj = (JSONObject) jsonParser.parse(response.getBody());
	        jsonArr = (JSONArray) jsonObj.get("data");
	        for(int i = 0; i < jsonArr.size(); i++) {
	        	pageJson = (JSONObject) jsonArr.get(i);
	        	pageAccessToken = (String) pageJson.get("access_token");
	        	break;
	        }
	        servletRequest.getSession().setAttribute("pageAccessToken", pageAccessToken);
	        
	        rmiServer = rmiClient.getRmi();
		   if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
			   Election objElection = rmiServer.getSpecificElection(title);
			   objElection = Utility.processElectionData(objElection);
			   Utility.postInFB(pageAccessToken, objElection);
		   }
	    } catch (Exception e) {
	    	e.printStackTrace();
//	    	servletResponse.sendRedirect("/oauthFail");
	    }
	    return new ModelAndView("redirect:ElectionDetails?title="+title);
	}   
	
	@RequestMapping(value = "/ShareResultInFB", method = RequestMethod.GET)
   public ModelAndView shareResult(HttpServletRequest request, @RequestParam("title")String title) {
	   ModelAndView modView = null;
	   Election objElection = null;
	   try {
		   rmiServer = rmiClient.getRmi();
		   if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
			   objElection = rmiServer.getSpecificElection(title);
			   
			   if(objElection != null) {
				   objElection = Utility.processElectionData(objElection);
				   if(request.getSession().getAttribute("pageAccessToken") != null) {
						pageAccessToken = (String) request.getSession().getAttribute("pageAccessToken");
						Utility.postInFB(pageAccessToken, objElection);
						modView = new ModelAndView("redirect:ElectionDetails?title="+title);
					} else {
						modView = new ModelAndView("facebook?page=");
					}
			   }
		   }
	   } catch(Exception e) {
		   e.printStackTrace();
	   }
      return modView;
   }
}
