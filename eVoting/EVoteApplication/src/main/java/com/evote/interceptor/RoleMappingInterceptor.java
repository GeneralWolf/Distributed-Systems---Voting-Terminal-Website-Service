package com.evote.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
 
public class RoleMappingInterceptor extends HandlerInterceptorAdapter
{
	StringBuffer voterAllowedURLs = new StringBuffer()									//string com todos os urls que um voter normal pode aceder
			.append("/Login /processLogin /Register /processRegistration /Elections ")
			.append("/ElectionDetails /AddVote /Logout /ShareResultInFB /facebook /facebook-callback");
    @Override
    public boolean preHandle(HttpServletRequest request,						//
            HttpServletResponse response, Object handler) throws Exception {
    	boolean containsURI = false;
    	String tempURI = null;
    	String role = (String) request.getSession().getAttribute("userRole");	//busca o role do user atual
    	String requestURI = request.getRequestURI();								//endereço URI
//       System.out.println("Request URI : "+requestURI);
       
    	if("Voter".equalsIgnoreCase(role)) {										//se for um voter só o permite aceder as paginas permitidas ao proibir o acesso as paginas nao contidas em voterAllowedUrls
    		tempURI = requestURI.substring(requestURI.lastIndexOf("/"), requestURI.length());
//    		System.out.println("tempURI ** : "+tempURI);
    		containsURI = voterAllowedURLs.toString().contains(tempURI);
    		if(!containsURI) {
    			response.sendError(HttpServletResponse.SC_FORBIDDEN);
    			return false;
    		}
    	} 
        return true;
    }
}