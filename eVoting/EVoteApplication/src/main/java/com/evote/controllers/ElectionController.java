package com.evote.controllers;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.evote.models.Election;
import com.evote.models.List;
import com.evote.models.User;
import com.evote.models.Vote;
import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;
import com.evote.util.Utility;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.GraphResponse;

@Controller
@RequestMapping("/jsp")
public class ElectionController { 
	@Autowired
	private RmiClient rmiClient;
	public static RmiServer rmiServer;

	/**
	 * Quando recebe um pedido para criar eleição da return de um modelAndView do MVC para esse pedido
	 * é redirecionado para aqui se na função processCreateElection ocorrer algum erro(?)
	 * @return
	 */
   @RequestMapping(value = "/CreateElection", method = RequestMethod.GET)
   public ModelAndView createElection() {
	  ModelAndView modView = new ModelAndView("CreateElection");
	  return modView;
   }

	/**
	 *Tenta criar uma eleição com os parametros dados como argumentos e tratar
	 * das falhas que possam acontecer
	 * @param objElection
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processCreateElection", params="CreateElection", method = RequestMethod.POST)
	public ModelAndView processCreateElection(@ModelAttribute("createElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		System.out.println("election candidate : "+objElection.getCandidateList());
//		System.out.println("election title : "+objElection.getTitle());
		String reply = null;
		ModelAndView modView = null;
		ArrayList<List> lstCandidates = null;
		try {
			lstCandidates = getCandidatesAsList(objElection.getCandidateList());	//obtem a lista de candidatos
			System.out.println("lstCandidates : "+lstCandidates);
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {	//verifica se o rmi server foi encontrado com sucesso
				
				reply = rmiServer.createElection(objElection.getTitle(), objElection.getDescription(), objElection.getStartDate(), objElection.getEndDate(), objElection.getStartHour(), objElection.getStartMinute(), objElection.getEndHour(), objElection.getEndMinute(), objElection.getType(), lstCandidates);
				System.out.println("After creating election, result : "+reply);		//^uma eleição é criada com os parâmetros dados no argumento da função
				
				if(reply == null || !"Election created".equalsIgnoreCase(reply)) {						//tudo abaixo é verificação da criação da eleição/ligação ao rmi
					modView = new ModelAndView("redirect:CreateElection");
					redirectAttr.addFlashAttribute("errorMsg", "New Election is not created!!");	//eleição nao foi criada com sucesso
				} else 
					//modView = new ModelAndView("ElectionDetails?title="+objElection.getTitle());
					modView = new ModelAndView("redirect:Elections");							//eleição foi criada com sucesso
			} else {
				modView = new ModelAndView("redirect:CreateElection");
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");	//ligação rmi nao foi estabelecida
			}
		} catch(RemoteException re) {
			re.printStackTrace();																				//ligação rmi falhou
			modView = new ModelAndView("redirect:CreateElection");
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * converte a lista dos candidatos de string para lista
	 * @param data
	 * @return
	 */
	private ArrayList<List> getCandidatesAsList(String data) {
		ArrayList<List> arrList = new ArrayList<List>();
		List candidateObj = null;
		String[] strArr = data.trim().split("\n");
		String[] nameTypeVal = null;
		for(int i = 0; i < strArr.length; i++) {
			nameTypeVal = strArr[i].trim().split("/");
			candidateObj = new List(nameTypeVal[0], nameTypeVal[1], null);
			arrList.add(candidateObj);
		}
		return arrList;
	}

	/**
	 * Busca eleições e devolve-as incluindo os seus estados
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/Elections", method = RequestMethod.GET)
   public ModelAndView getElections(HttpServletRequest request) {
	  ModelAndView modView = new ModelAndView("Elections");
		ArrayList<Election> lstElections = null;
		ArrayList<Election> lstAllElections = null;
		ArrayList<Election> lstUserElections = null;
		ArrayList<Election> lstElectionResult = null;
		Election objElection = null;
		String userRole = null;
		String userType = null;
		//TODO Remove commenting and populate hardcoded values
		try {
			rmiServer = rmiClient.getRmi();													//faz ligação rmi
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				lstAllElections = rmiServer.getElections();									//busca todas as eleições
		
//				lstElections = new ArrayList<Election>();
//				lstElections.add(Utility.populateElectionData());
//				lstElections.add(Utility.populateElectionData());
		
				System.out.println("All election : "+lstAllElections.size());
				if(lstAllElections != null && !lstAllElections.isEmpty()) {
					userRole = (String) request.getSession().getAttribute("userRole");	//busca dados sobre o user que esta ligado na sessao
					userType = (String) request.getSession().getAttribute("userType");
					System.out.println("user role and user type *** "+userRole+ " *** "+userType);
					if(userRole != null && "Voter".equalsIgnoreCase(userRole)) {
						lstUserElections = Utility.getElectionsForUser(lstAllElections, userType);	//busca eleiçoes que o user pode votar
						lstElections = lstUserElections;
					} else
						lstElections = lstAllElections;												//se for admin ve todas
					
					lstElectionResult = new ArrayList<Election>();
					for(int i = 0; i < lstElections.size(); i++) {
						objElection = Utility.processElectionData(lstElections.get(i));
						lstElectionResult.add(objElection);											//guarda os estados das eleições
					}
					modView.addObject("allElections", lstElectionResult);				//guarda no modView os resultados das eleições(incluindo as inacabadas)
				} else {
					modView.addObject("errorMsg", "No elections found!!");		//abaixo é so em caso de exceções
				}
			} else {
				modView.addObject("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			modView.addObject("errorMsg", "Could not connect to server. Please try after some time!!");
		}
	  return modView;
   }

	/**
	 *Verifica se o utilizador atual já votou numa eleição e devolve tanto a eleição como o facto de ele ter votado ou nao
	 * @param title
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/ElectionDetails", method = RequestMethod.GET)
   public ModelAndView getElectionDetails(@RequestParam("title")String title, HttpServletRequest request, RedirectAttributes redirectAttr) {
		System.out.println("request param title : "+title);
		ModelAndView modView = null;
		Election objElection = null;
		ArrayList<User> lstUser = null;
		boolean isUserVoted = false;
		String currentUser = (String) request.getSession().getAttribute("username");		//busca username atual
		//TODO - Remove commenting once tested
		try {
			request.getSession().setAttribute("selectedElection", title);
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				objElection = rmiServer.getSpecificElection(title);							//busca a eleição procurada
				
//		objElection = Utility.populateElectionData();
		
		
				if(objElection != null) {
					modView = new ModelAndView("ElectionDetails");
					lstUser = rmiServer.getUsers();
					isUserVoted = Utility.checkUserVotedForElection(currentUser, title, lstUser);
					objElection = Utility.processElectionData(objElection);
					modView.addObject("election", objElection);					//adiciona a modview a eleiçao e se o user votou
					modView.addObject("isUserVoted", isUserVoted);
				} else {
					modView = new ModelAndView("redirect:Elections");				//em caso de erro redireciona para Elections
					redirectAttr.addFlashAttribute("errorMsg", "There is no such election configured!");			//tudo abaixo são exceçoes
				}
			} else {
				modView = new ModelAndView("redirect:Elections");
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			re.printStackTrace();
			modView = new ModelAndView("redirect:Elections");
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
   }

	/**
	 * muda o titulo de uma eleição
	 * @param objElection
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processEditElection", params="editTitle", method = RequestMethod.POST)
	public ModelAndView processEditTitle(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
//		System.out.println("election candidate : "+objElection.getCandidateList());
		System.out.println("updated election title : "+objElection.getTitle());
		
		ModelAndView modView = processEditElection(objElection, 1, objElection.getTitle(), redirectAttr);
		return modView;
	}

	/**
	 * edita descriçao da eleiçao
	 * @param objElection
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/processEditElection", params="editDescription", method = RequestMethod.POST)
	public ModelAndView processEditDesc(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 2, objElection.getDescription(), redirectAttr);
		return modView;
	}
	
	@RequestMapping(value = "/processEditElection", params="editStartDate", method = RequestMethod.POST)
	public ModelAndView processEditStartDate(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 3, objElection.getStartDate(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editEndDate", method = RequestMethod.POST)
	public ModelAndView processEditEndDate(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 4, objElection.getEndDate(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editStartHour", method = RequestMethod.POST)
	public ModelAndView processEditStartHour(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 5, objElection.getStartHour(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editStartMinute", method = RequestMethod.POST)
	public ModelAndView processEditStartMinute(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 6, objElection.getStartMinute(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editEndHour", method = RequestMethod.POST)
	public ModelAndView processEditEndHour(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 7, objElection.getEndHour(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editEndMinute", method = RequestMethod.POST)
	public ModelAndView processEditEndMinute(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 8, objElection.getEndMinute(), redirectAttr);
		return modView;
	}
	@RequestMapping(value = "/processEditElection", params="editType", method = RequestMethod.POST)
	public ModelAndView processEditType(@ModelAttribute("editElectionForm") Election objElection, RedirectAttributes redirectAttr) {
		ModelAndView modView = processEditElection(objElection, 9, objElection.getType(), redirectAttr);
		return modView;
	}

	/**
	 * função geral para se editar uma eleição
	 * @param objElection
	 * @param field
	 * @param newValue
	 * @param redirectAttr
	 * @return
	 */
	private ModelAndView processEditElection(Election objElection, int field, String newValue, RedirectAttributes redirectAttr) {
		System.out.println("fiels to update "+field);
		String reply = null;
		ModelAndView modView = new ModelAndView("redirect:ElectionDetails?title="+objElection.getTitle());
//		modView.addObject("title", objElection.getTitle());
		
		try {
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				reply = rmiServer.editElection(objElection, field, newValue);				//chama a funçao rmi para editar uma eleição
				if(reply == null || "Elections unchanged".equalsIgnoreCase(reply)) {		//abaixo é tratamento de exceções
					redirectAttr.addFlashAttribute("errorMsg", "Election is not updated!!");
				} else 
					redirectAttr.addFlashAttribute("errorMsg", "Election Details got updated successfully!!");
			} else {
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * Função para se votar
	 * @param title
	 * @param name
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
	@RequestMapping(value = "/AddVote", method = RequestMethod.GET)
	public ModelAndView addVote(@RequestParam("title")String title, @RequestParam("name")String name, HttpServletRequest request, RedirectAttributes redirectAttr) {
//		System.out.println("inside addvote ********** title :"+title+ " **** name : "+name);
		ModelAndView modView = null;
		String pageAccessToken = null;
		Vote objVote = null;
		Election objElection = null;
		List candidate = null;
		boolean isWhiteVote = false;
		boolean isNullVote = false;
		String candidateName = null;
		User objUser = null;
		String username  = null;
		String department = null;
		ArrayList<User> lstUsers = null;
		int index = 0;
		try {
			username = (String) request.getSession().getAttribute("username");			//busca o nome e departamento do user atraves da sessão
			department = (String) request.getSession().getAttribute("userDepartment");
			
			if(name != null) {								//verifica se o voto é branco ou nulo através do nome dado como argumento da função
				if("White".equalsIgnoreCase(name))
					isWhiteVote = true;
				else if("Null".equalsIgnoreCase(name))
					isNullVote = true;
				else
					candidateName = name;
			}
						
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {	//vai buscar a eleição especifica e os seus detalhes
				objElection = rmiServer.getSpecificElection(title);
				objElection = Utility.processElectionData(objElection);
				
				if(candidateName != null) {
					candidate = rmiServer.getList(candidateName);
					if(candidate != null) {
						objUser = new User();
						objUser.setName(username);
						objUser.setDepartment(department);
						if(candidate.getUsers() != null)
							candidate.getUsers().add(objUser);
						else {
							lstUsers = new ArrayList<User>();
							lstUsers.add(objUser);
							candidate.setUsers(lstUsers);
						}
						
						for(List objList : objElection.getLists()) {
							if(objList.getName().equalsIgnoreCase(candidateName)) {
								break;
							} else
								index++;
						}
						objElection.getLists().get(index).getUsers().add(objUser);
					}
				} else 
					candidate = null;
				
				objVote = new Vote(objElection, candidate, department, LocalDateTime.now(), isNullVote, isWhiteVote);	//guarda o voto como objeto para ser usado em funções
				rmiServer.addVote(objVote);																				//adiciona o voto e a eleiçao as eleiçoes em que o user votou
				rmiServer.addUserElection(username, objElection);
				
				redirectAttr.addFlashAttribute("isUserVotedNow", true);
				redirectAttr.addFlashAttribute("errorMsg", "You have voted successfully!!");
				
				if(request.getSession().getAttribute("pageAccessToken") != null) {
					pageAccessToken = (String) request.getSession().getAttribute("pageAccessToken");
					Utility.postInFB(pageAccessToken, objElection);											//chama a funçao para se postar no facebook
				} else {
					modView = new ModelAndView("facebook?page=");									//tratamento de exceções abaixo
				}
			} else {
				modView = new ModelAndView("redirect:ElectionDetails?title="+title);
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			modView = new ModelAndView("redirect:ElectionDetails?title="+title);
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}
}