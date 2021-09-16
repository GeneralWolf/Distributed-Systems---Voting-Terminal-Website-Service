package com.evote.controllers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.evote.models.Election;
import com.evote.models.User;
import com.evote.models.VotingTable;
import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;
import com.evote.util.Utility;

@Controller
@RequestMapping("/jsp")
public class VotingTableController { 
	@Autowired
	private RmiClient rmiClient;
	public static RmiServer rmiServer;

	/**
	 *Vai buscar as voting tables
	 * @param title
	 * @param request
	 * @return
	 */
   @RequestMapping(value = "/VotingTables", method = RequestMethod.GET)
   public ModelAndView getVotingTables(@RequestParam("title")String title, HttpServletRequest request) {
	  ModelAndView modView = new ModelAndView("VotingTables");;
	  
	  CopyOnWriteArrayList<VotingTable> lstVotingTabs = null;
	  CopyOnWriteArrayList<VotingTable> lstVotingTables = null;
		
		//TODO - Remove commenting once tested
		try {
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				lstVotingTabs = rmiServer.getVotingTables();			//busca todas as voting tables
				System.out.println("lstVotingTabs : "+lstVotingTabs);
//		lstVotingTabs = Utility.populateVotingTables();
		
				if(lstVotingTabs != null && !lstVotingTabs.isEmpty()) {				//se as voting tables nao forem nulas
					lstVotingTables = Utility.getAssoVotingTables(lstVotingTabs, title);		//busca as voting tables associadas a uma eleição
					modView.addObject("listVotingTables", lstVotingTables);			//guarda as voting tables na modView
				} else {
					modView.addObject("errorMsg", "No Voting tables configured!");
				}
				request.getSession().setAttribute("title",title);
			} else {
				modView.addObject("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			modView.addObject("errorMsg", "Could not connect to server. Please try after some time!!");
		}
	  return modView;
   }

	/**
	 * Adiciona uma eleição a uma mesa de voto
	 * @param votingTable
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
   @RequestMapping(value = "/AddVotingTable", params="addVotingTable", method = RequestMethod.POST)
	public ModelAndView addVotingTable(@ModelAttribute("addVotingTableForm") VotingTable votingTable, HttpServletRequest request, RedirectAttributes redirectAttr) {
		String reply = null;
		ModelAndView modView = new ModelAndView();
		String electionTitle = null;
		Election objElection = new Election();
		try {
			electionTitle = (String) request.getSession().getAttribute("title");
  			modView.setViewName("redirect:VotingTables?title="+electionTitle);
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				objElection.setTitle((String) request.getSession().getAttribute("title"));		//busca o titulo da sessao
				reply = rmiServer.manageVotingTables(votingTable, objElection, "add");		//adiciona a eleição à mesa de voto
				if(reply == null || !"New voting table is added".equalsIgnoreCase(reply)) {			//eleição foi adicionada com sucesso?
					redirectAttr.addFlashAttribute("errorMsg", "Adding voting table is failed!!");	//abaixo é tratamento de exceções
				} else {
					redirectAttr.addFlashAttribute("errorMsg", reply);
				}
			} else {
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 *Função semelhante à de cima
	 * @param department
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
   @RequestMapping(value = "/AddVotTableToElection", method = RequestMethod.GET)
	public ModelAndView addVotTableToElection(@RequestParam("department")String department, HttpServletRequest request, RedirectAttributes redirectAttr) {
		String reply = null;
		ModelAndView modView = new ModelAndView();
		String electionTitle = null;
		Election objElection = new Election();
		VotingTable votingTable = new VotingTable();
		try {
			electionTitle = (String) request.getSession().getAttribute("title");		//busca o titulo da eleição com o titulo do pedido
 			modView.setViewName("redirect:VotingTables?title="+electionTitle);
			rmiServer = rmiClient.getRmi();
			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
				objElection.setTitle((String) request.getSession().getAttribute("title"));	//muda o titulo da eleiçao de acordo com o titulo do pedido
				votingTable.setDepartment(department);											//declara o departamento da mesa de voto
				reply = rmiServer.manageVotingTables(votingTable, objElection, "add");	//adiciona a eleição à mesa de voto
				if(reply == null || !"Election added to voting table".equalsIgnoreCase(reply)) {		//abaixo é so tratamento de exceções
					redirectAttr.addFlashAttribute("errorMsg", "Adding voting table is failed!!");
				} else {
					redirectAttr.addFlashAttribute("errorMsg", reply);
				}
			} else {
				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
			}
		} catch(RemoteException re) {
			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
		}
		return modView;
	}

	/**
	 * Função que remove uma eleição de uma mesa de voto
	 * @param department
	 * @param request
	 * @param redirectAttr
	 * @return
	 */
   @RequestMapping(value = "/RemoveVotingTable", method = RequestMethod.GET)
  	public ModelAndView removeVotingTable(@RequestParam("department")String department, HttpServletRequest request, RedirectAttributes redirectAttr) {
  		String reply = null;
  		String electionTitle = null;
  		ModelAndView modView = new ModelAndView();
  		Election objElection = new Election();
  		VotingTable votingTable = new VotingTable();
//  		System.out.println("Election title in remove voting table : "+(String) request.getSession().getAttribute("title"));
  		try {
  			electionTitle = (String) request.getSession().getAttribute("title");
  			modView.setViewName("redirect:VotingTables?title="+electionTitle);
  			rmiServer = rmiClient.getRmi();
  			if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
  				objElection.setTitle(electionTitle);
  				
  				votingTable.setDepartment(department);
  				reply = rmiServer.manageVotingTables(votingTable, objElection, "remove");
  				if(reply == null || !"Election removed from voting table".equalsIgnoreCase(reply)) {
  					redirectAttr.addFlashAttribute("errorMsg", "Removing voting table is failed!!");
  				} else {
  					redirectAttr.addFlashAttribute("errorMsg", reply);
  				}
  			} else {
  				redirectAttr.addFlashAttribute("errorMsg", "Server Down. Please try after some time!!");
  			}
  		} catch(RemoteException re) {
  			redirectAttr.addFlashAttribute("errorMsg", "Could not connect to server. Please try after some time!!");
  		}
  		return modView;
  	}
}
