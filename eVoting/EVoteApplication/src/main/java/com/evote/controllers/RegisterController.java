package com.evote.controllers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.evote.models.User;
import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;

@Controller
@RequestMapping("/jsp")
public class RegisterController { 
	@Autowired
	private RmiClient rmiClient;
	public static RmiServer rmiServer;

	/**
	 * cria um novo ModelAndView com o nome Register
	 * @return
	 */
	@RequestMapping(value = "/Register", method = RequestMethod.GET)
   public ModelAndView register() {
	   ModelAndView modView = new ModelAndView("Register");
      return modView;
   }

	/**
	 *Função para tentar registar um utilizador
	 * @param user
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processRegistration", params="Register", method = RequestMethod.POST)		//registo de users
	public ModelAndView registration(@ModelAttribute("registrationForm") User user, HttpServletRequest request, RedirectAttributes redirectAttr) {
//		System.out.println("Üsername in registration form: "+user.getName());
//		System.out.println("Password in registration form: "+user.getPassword());
		System.out.println("username in session "+request.getSession().getAttribute("username")+" **** "+user.getRole());
		String reply = null;
		ModelAndView modView = null;
		
		try {
			if(request.getSession().getAttribute("username") == null &&		//se user for admin e nao tiver username
					"Admin".equalsIgnoreCase(user.getRole())) {
				modView = new ModelAndView("Login");
				modView.addObject("errorMsg", "Only Admin can create admin user!! Please login as admin and try registering admin user.");		//falha ao criar
			} else {
				rmiServer = rmiClient.getRmi();
				if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
					reply = rmiServer.registerUser(user.getName(), user.getRole(), user.getType(), user.getPassword(),user.getDepartment(), user.getPhoneNumber(), user.getResidenceAddress(), user.getCCNumber(), user.getCCExpirationDate());
					if(reply != null && "User registered".equalsIgnoreCase(reply)) {		//se o registo do user novo tiver sido um sucesso
						modView = new ModelAndView("Login");						//redireciona o user para a pagina de login
						modView.addObject("errorMsg", "Successfully Registered!! Please login.");
					} else {																//abaixo é tratamento de exceções
						modView = new ModelAndView("redirect:Register");
						redirectAttr.addFlashAttribute("errorMsg", "Registration Failed!!");
					}
				} else {
					modView = new ModelAndView("redirect:Register");
					redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
				}
			}
		} catch(RemoteException re) {
			modView = new ModelAndView("redirect:Register");
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * Semelhante à função de cima mas tem inicio diferente (request diferente)
	 * @param user
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processFBRegistration", params="Register", method = RequestMethod.POST)
	public ModelAndView fbRegistration(@ModelAttribute("fbRegistrationForm") User user, HttpServletRequest request, RedirectAttributes redirectAttr) {
		System.out.println("username in session "+request.getSession().getAttribute("username")+" **** "+user.getRole());
		String reply = null;
		ModelAndView modView = null;
		
		try {
			if(request.getSession().getAttribute("username") == null &&		//se o user atual for um admin sem username
					"Admin".equalsIgnoreCase(user.getRole())) {
				modView = new ModelAndView("Login");		//da erro e user é mandado para ecra de login
				modView.addObject("errorMsg", "Only Admin can create admin user!! Please login as admin and try registering admin user.");
			} else {
				rmiServer = rmiClient.getRmi();
				if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
					reply = rmiServer.registerFacebookUser(user.getName(), user.getUid(), user.getRole(), user.getType(), user.getDepartment(), user.getPhoneNumber(), user.getResidenceAddress(), user.getCCNumber(), user.getCCExpirationDate());
					if(reply != null && "User registered".equalsIgnoreCase(reply)) {		//registo de user foi um sucesso
						if("Admin".equalsIgnoreCase(user.getRole()))					//se user é um admin
							modView = new ModelAndView("AdminPage");			//é encaminhado para página de admins
						else
							modView = new ModelAndView("redirect:Elections");	//caso contrario, é encaminhado para pagina de eleições
						
						modView.addObject("errorMsg", "Successfully Registered!!");		//aqui e para baixo é tratamento de exceções
					} else {
						modView = new ModelAndView("redirect:Register");
						redirectAttr.addFlashAttribute("errorMsg", "Registration Failed!!");
					}
				} else {
					modView = new ModelAndView("redirect:Register");
					redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
				}
			}
		} catch(RemoteException re) {
			modView = new ModelAndView("redirect:Register");
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}
}