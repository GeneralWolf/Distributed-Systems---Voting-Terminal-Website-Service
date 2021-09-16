package com.evote.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Value;

import com.evote.models.Election;
import com.evote.models.List;
import com.evote.models.User;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.GraphResponse;

public class Utility {
	@Value("http://localhost:8080/EVoteApplication/jsp/ElectionDetails.jsp?title=")
	private static String fbLinkInPost;
	
	static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");  
	
	public static User getUserData(String username, ArrayList<User> lstUsers) {
		for(User user : lstUsers) {
			if(user.getName().equalsIgnoreCase(username)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Atualiza o estado da eleição usando a função getElectionStatus para verificar de acordo com a data atual se a eleição está por começar/a meio/acabada
	 * @param objElection
	 * @return
	 */
	public static Election processElectionData(Election objElection) {
		Election updatedElectionObj = objElection;
		String status = null;

		status = getElectionStatus(updatedElectionObj);
		updatedElectionObj.setStatus(status);
		if(status != null && "Done".equalsIgnoreCase(status))
			updateWinnerData(updatedElectionObj);
		else {
			updatedElectionObj.setWonBy("NA");
			updatedElectionObj.setPercentWinVotes("NA");
		}
		return updatedElectionObj;
	}

	/**
	 * Verifica se a eleição está para começar/a meio/acabada de acordo com as suas datas de começo e fim
	 * @param electionObj
	 * @return
	 */
	static String getElectionStatus(Election electionObj) {
		String status = null;
		String strStartDateTime = null;
		String strEndDateTime = null;
		Calendar calCurrent = null;
		Calendar calStart = null;
		Calendar calEnd = null;
		Date dateStart = null;
		Date dateEnd = null;
		try {
			strStartDateTime = electionObj.getStartDate()+" "+electionObj.getStartHour()+":"+electionObj.getStartMinute();
			dateStart = formatter.parse(strStartDateTime);
			calStart = Calendar.getInstance();
			calStart.setTime(dateStart);
//			System.out.println("Calendar start date time : "+calStart.getTime());
			
			strEndDateTime = electionObj.getEndDate()+" "+electionObj.getEndHour()+":"+electionObj.getEndMinute();
			dateEnd = formatter.parse(strEndDateTime);
			calEnd = Calendar.getInstance();
			calEnd.setTime(dateEnd);
//			System.out.println("Calendar end date time : "+calEnd.getTime());
			
			calCurrent = Calendar.getInstance();
//			System.out.println("Calendar current date time : "+calCurrent.getTime());
			
			if(calCurrent.before(calStart)) {
				status = "YetToStart";
			} else if(calCurrent.before(calEnd)) {
				status = "Ongoing";
			} else {
				status = "Done";
			}
		} catch(Exception e) {
			status = "NA";
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Verifica o vencedor de uma eleição
	 * @param electionObj
	 */
	static void updateWinnerData(Election electionObj) {
		List candidate = null;
		int noOfVotes = 0;
		int maxVote = 0;
		int totalVote = 0;
		int totalCandidates = 0;
		String candidateName = null;
		int percentWinVotes = 0;
		
		ArrayList<List> lstList = electionObj.getLists();
		if(lstList != null && !lstList.isEmpty()) {
			totalCandidates = lstList.size();
			for(int i = 0; i < totalCandidates; i++) {
				candidate = lstList.get(i);
				if(candidate != null) {
					noOfVotes = (candidate.getUsers() != null) ? candidate.getUsers().size() : 0;
					totalVote = totalVote + noOfVotes;
					if(noOfVotes > maxVote) {
						maxVote = noOfVotes;
						candidateName = candidate.getName();
					}
				}
			}
			electionObj.setWonBy(candidateName);
			electionObj.setTotalVotes(String.valueOf(totalVote));
//			System.out.println("Won : "+candidateName+" *** vote : "+maxVote+" *** totalVote : "+totalVote);
			percentWinVotes = (int)(((float) maxVote / (float) totalVote) * 100);
			electionObj.setPercentWinVotes(""+percentWinVotes);
		} else {
			electionObj.setWonBy("NA");
			electionObj.setPercentWinVotes("NA");
		}
		
	}

	/**
	 * Retorna as mesas de voto associas a uma eleição
	 * @param arrLst
	 * @param title
	 * @return
	 */
	public static CopyOnWriteArrayList<VotingTable> getAssoVotingTables(CopyOnWriteArrayList<VotingTable> arrLst, String title) {
		VotingTable votTable = null;
		ArrayList<Election> lstElection = null;
		CopyOnWriteArrayList<VotingTable> lstVotTable = new CopyOnWriteArrayList<VotingTable>();
		
		for(int i = 0; i < arrLst.size(); i++) {
			votTable = arrLst.get(i);
			lstElection = votTable.getElections();
			for(int j = 0; j < lstElection.size(); j++) {
				if(title.equalsIgnoreCase(lstElection.get(j).getTitle())) {
					votTable.setAddedToElection(true);
				} else {
					votTable.setAddedToElection(false);
				}
				lstVotTable.add(votTable);
			}
		}
		return lstVotTable;
	}

	/**
	 * Verifica se um utilizador já votou numa eleição
	 * @param username
	 * @param title
	 * @param lstUsers
	 * @return
	 */
	public static boolean checkUserVotedForElection(String username, String title, ArrayList<User> lstUsers) {
		boolean isUserVoted = false;
		
		for(User objUser : lstUsers) {
			if(objUser.getName().equalsIgnoreCase(username)) {
				for(Election objElection : objUser.getElections()) {
					if(objElection.getTitle().equalsIgnoreCase(title)) {
						return true;
					}
				}
			}
		}
		
		return isUserVoted;
	}

	/**
	 * Retorna todas as eleições do mesmo tipo do utilizador
	 * @param lstAllElections
	 * @param userType
	 * @return
	 */
	public static ArrayList<Election> getElectionsForUser(ArrayList<Election> lstAllElections, String userType) {
		ArrayList<Election> lstUserElections = new ArrayList<Election>();
		for(Election objElection : lstAllElections) {
			System.out.println("election type "+objElection.getType());
			if(objElection.getType().equalsIgnoreCase(userType))
				lstUserElections.add(objElection);
		}
		System.out.println("user elections size "+lstUserElections.size());
		return lstUserElections;
	}

	/**
	 * Função responsavel por fazer o post no facebook sobre uma eleição, atraves da função publish do API do facebook (distingue entre eleições a ocorrer e terminadas)
	 * @param pageAccessToken
	 * @param objElection
	 */
	public static void postInFB(String pageAccessToken, Election objElection) {
		String message = null;
		try {
			if("Ongoing".equalsIgnoreCase(objElection.getStatus())) {
				message = "I have voted in the election!!";
			} else if("Done".equalsIgnoreCase(objElection.getStatus())) {
				message = "Election "+objElection.getTitle()+" - Won by "+objElection.getWonBy()+" with "+objElection.getPercentWinVotes()+"%";
			}
			FacebookClient facebookClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
	        GraphResponse publishMessageResponse =
			  facebookClient.publish("me/feed", GraphResponse.class,
			    Parameter.with("message", message),
	//		    Parameter.with("link", "www.google.com"));
			    Parameter.with("link", fbLinkInPost+objElection.getTitle()));
	//		    Parameter.with("name", "ElectionDetails"),
	//		    Parameter.with("caption", "Election Details"),
	//		    Parameter.with("description", "Description"));
	
			System.out.println("Published message ID: " + publishMessageResponse.getId());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Função usada para criar uma eleiçao mockup para testes
	 * @return
	 */
	public static Election populateElectionData() {
		Election objElec = new Election();
		objElec.setTitle("Election1");
		objElec.setDescription("Description about election 1");

		ArrayList arrList = new ArrayList();
		ArrayList usrlst = new ArrayList();
		User userObj = new User();
		userObj.setName("user1");
		usrlst.add(userObj);
		List lst = null;
		for(int i = 0; i < 4; i++) {
			lst = new List("name "+i,"type "+i,usrlst);
			arrList.add(lst);
		}
		objElec.setLists(arrList);
		objElec.setStartDate("11-05-2021");
		objElec.setStartHour("10");
		objElec.setStartMinute("05");
		objElec.setEndDate("11-05-2021");
		objElec.setEndHour("18");
		objElec.setEndMinute("05");
		return objElec;
	}

	/**
	 * funçao para criaçao de mockups para teste
	 * @return
	 */
	public static CopyOnWriteArrayList<VotingTable> populateVotingTables() {
		CopyOnWriteArrayList<VotingTable> lstVotingTbl = new CopyOnWriteArrayList<VotingTable>();
		VotingTable votTable = null;
		Election objElec = new Election();
		ArrayList arrList = new ArrayList();
		ArrayList usrlst = new ArrayList();
		CopyOnWriteArrayList<VotingTerminal> lstVotTerminal = new CopyOnWriteArrayList<VotingTerminal>();
		VotingTerminal votTerminal = null;
		User userObj = new User();
		List lst = null;
		
		objElec.setTitle("Election1");
		objElec.setDescription("Description about election 1");
		userObj.setName("user1");
		usrlst.add(userObj);
		for(int i = 0; i < 4; i++) {
			lst = new List("name "+i,"type "+i,usrlst);
			arrList.add(lst);
		}
		objElec.setLists(arrList);
		objElec.setStartDate("23-06-2021");
		objElec.setStartHour("10");
		objElec.setStartMinute("05");
		objElec.setEndDate("23-06-2021");
		objElec.setEndHour("18");
		objElec.setEndMinute("05");
		
		
		votTable = new VotingTable();
		votTable.setDepartment("Dept1");
		votTable.addElection(objElec);
		votTerminal = new VotingTerminal(1,true);
		lstVotTerminal.add(votTerminal);
		votTerminal = new VotingTerminal(2,false);
		lstVotTerminal.add(votTerminal);
		
		votTable.setVotingTerminals(lstVotTerminal);
		lstVotingTbl.add(votTable);
		
		votTable = new VotingTable();
		votTable.setDepartment("Dept2");
		votTable.addElection(objElec);
		lstVotingTbl.add(votTable);
		
		return lstVotingTbl;
	}
	
	public static void main( String args[]) {
		
//		getElectionStatus(objElec);
		//Election obj = processElectionData(populateElectionData());
		//System.out.println(obj.getWonBy()+ " *** "+obj.getPercentWinVotes());
	}
}
