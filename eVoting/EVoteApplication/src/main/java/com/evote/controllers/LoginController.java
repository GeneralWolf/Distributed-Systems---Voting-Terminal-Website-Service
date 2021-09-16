package com.evote.controllers;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.evote.models.User;
import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;
import com.evote.util.Utility;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@RequestMapping("/jsp")
public class LoginController { 
	@Autowired
	private RmiClient rmiClient;
	
	public static RmiServer rmiServer;

	/**
	 * cria objeto ModelAndView quando o user é redirecionado para login
	 * @return
	 */
   @RequestMapping(value = "/Login", method = RequestMethod.GET)
   public ModelAndView login() {
	   ModelAndView modView = new ModelAndView("Login");
	   /*User newUser = new User();
	   newUser.setUserName("ABCD");
	   newUser.setPassword("pw123");
	   
	   modView.addObject("user", newUser);*/
      return modView;
   }

	/**
	 * Função de login que permite verificar os dados dados como input pelo user e guardar os dados deste na sessão
	 * @param user
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processLogin", params="Login", method = RequestMethod.POST)
	public ModelAndView login(@ModelAttribute("loginForm") User user, HttpServletRequest request, RedirectAttributes redirectAttr) {
//		System.out.println("Üsername : "+user.getName());
//		System.out.println("Password : "+user.getPassword());
//		System.out.println("Role : "+user.getRole());
		boolean authenticated = false;
		String role = null;
		ModelAndView modView = null;
		ArrayList<User> lstUsers = null;
		User objUser = null;
		
		try {
			if(user.getRole() == null)		//estabelece o role do user se já nao estiver estabelecido
				role = "Voter";
			else
				role = "Admin";
			
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				authenticated = rmiServer.authenticateUser(user.getName(), user.getPassword(), role);		//verifica se o user existe e deu input dos dados corretos
				if(authenticated) {
					request.getSession().setAttribute("username", user.getName());						//guarda na sessao o nome e role do user atual
					request.getSession().setAttribute("userRole", role);
					if("Admin".equalsIgnoreCase(role))												//se for admin é levado para a pagina de administradores, caso contrario para a pagina de eleições
						modView = new ModelAndView("AdminPage");
					else
						modView = new ModelAndView("redirect:Elections");
					
					lstUsers = rmiServer.getUsers();
					if(lstUsers != null && !lstUsers.isEmpty()) {
						objUser = Utility.getUserData(user.getName(), lstUsers);					//busca a informação do user
						if(objUser != null) {
							request.getSession().setAttribute("userDepartment", objUser.getDepartment());		//guarda na sessao mais informação do user
							System.out.println("userType while loggin in "+objUser.getType());
							request.getSession().setAttribute("userType", objUser.getType());
						}																							//abaixo é tratamento de exceções
					}
				} else {
					modView = new ModelAndView("redirect:Login");
					redirectAttr.addFlashAttribute("errorMsg", "Username or Password is invalid!!");
				}
			} else {
				modView = new ModelAndView("redirect:Login");
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			modView = new ModelAndView("redirect:Login");
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * Função encarregada de dar o login do user apenas com base no seu ID, através do facebook(?)
	 * @param uid
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/FacebookLogin", method = RequestMethod.GET)
	public ModelAndView facebookLogin(@RequestParam("uid")String uid, HttpServletRequest request, RedirectAttributes redirectAttr) {
	  ModelAndView modView = null;
	  User userObj = null;
	  
	  try {
		  rmiServer = rmiClient.getRmi();
		  if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
			userObj = rmiServer.getValidUser(uid);											//busca o utilizador com base no ID dado
			if(userObj == null) {															//se nao tiver sido encontrado, é enviado para a pagina FBUserRegistration
				modView = new ModelAndView("FBUserRegistration");
				modView.addObject("uid", uid);
			} else {																		//caso contrario, os atributos da sessao são definidos de acordo com o user encontrado e o user é redirecionado para
				request.getSession().setAttribute("username", userObj.getName());		//a pagina "home" do seu tipo (admin -> pagina de administração, voter -> pagina de eleições)
				request.getSession().setAttribute("userDepartment", userObj.getDepartment());
				request.getSession().setAttribute("userType", userObj.getType());
				request.getSession().setAttribute("userRole", userObj.getRole());
				if("Admin".equalsIgnoreCase(userObj.getRole()))
					modView = new ModelAndView("AdminPage");
				else
					modView = new ModelAndView("redirect:Elections");
			}
		  }
	  } catch(RemoteException re) {															//tratamento de exceções
		  modView = new ModelAndView("redirect:Login");
		  redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
	  }
      return modView;
	}

	/**
	 * Associar uma conta de utilizador a uma conta de facebook atraves do uso do Id da conta
	 * @param uid
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/LinkFacebook", method = RequestMethod.GET)
	public ModelAndView linkFacebook(@RequestParam("uid")String uid, HttpServletRequest request) {
		ModelAndView modView = new ModelAndView("Elections");
		String reply = null;
		String username = null;
		String department = null;
		String role = null;
		String type = null;
	  
		try {
		  rmiServer = rmiClient.getRmi();
		  if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
			  	username = (String) request.getSession().getAttribute("username");				//vai buscar todos os dados do user a sessão
				department = (String) request.getSession().getAttribute("userDepartment");
				type = (String) request.getSession().getAttribute("userType");
				role = (String) request.getSession().getAttribute("userRole");
				System.out.println("Before linking facebook account, uid : "+uid+" ** name : "+username+" ** department : "+department+" ** type : "+type+" *** role : "+role);
				reply = rmiServer.linkFacebookUId(uid, username, role, type, department);				//associa o utilizador a uma conta de facebook
				System.out.println("Result of linking facebook "+reply);
				modView.addObject("errorMsg", reply);
		  }
		} catch(RemoteException re) {																	//tratamento de exceções
		  re.printStackTrace();
		  modView.addObject("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * Basicamente para redirecionar o utilizador para a pagina Admin
	 * @return
	 */
	@RequestMapping(value = "/AdminPage", method = RequestMethod.GET)
   public ModelAndView adminPage() {
	   ModelAndView modView = new ModelAndView("AdminPage");
      return modView;
   }

	/**
	 * Função para tratar do logout do utilizador e remover o username da sessão
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/Logout", method = RequestMethod.GET)
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		String username = null;
		try {
			if(request.getSession().getAttribute("username") != null) {
				username = (String) request.getSession().getAttribute("username");
		
				rmiServer = rmiClient.getRmi();
				if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive())))
					rmiServer.logout(username); 
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		request.getSession().invalidate();
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){    
	        new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
	    return "Login";
	}
}