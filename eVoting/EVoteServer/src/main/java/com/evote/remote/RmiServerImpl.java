package com.evote.remote;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.evote.models.Election;
import com.evote.models.List;
import com.evote.models.User;
import com.evote.models.Vote;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;

/**
 * O RMI corre começa por tentar perceber se existe algum servidor RMI ativo no porto
 * 7001 e, caso não exista, inicia o servidor. Caso se encontre um ativo fica a verficar
 * se o servidor principal vai abaixo. Caso isso aconteça, e se verifique que está em
 * baixo por um período de 5s, então o servidor secundário passa a principal.
 */
public class RmiServerImpl extends UnicastRemoteObject implements RmiServer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static String host = null;
    static String rmi_name = null;
    static int rmi_port = 0;
    static Registry r;
    static RmiServerImpl rmiServer;

    static ArrayList<User> users = new ArrayList<> ();
    static ArrayList<Election> elections = new ArrayList<> ();
    static ArrayList<List> lists = new ArrayList<> ();
    static ArrayList<Vote> votes = new ArrayList<> ();
    static CopyOnWriteArrayList<VotingTable> votingTables = new CopyOnWriteArrayList<> ();
    static Map<String, Integer> noOfVotesInElection = new HashMap<String, Integer>();
    static Map<String, Boolean> loggedInUsers = new HashMap<String, Boolean>();

    RmiServerImpl() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        RmiServer s;
        String message;
        int i = 1;

        if(readPropertiesFile(false)) {
            try {
                s = (RmiServer) LocateRegistry.getRegistry(host, rmi_port).lookup(rmi_name);
                while (i < 5) {

                    try {
                        Thread.sleep(1000);
                        message = s.isAlive();
                        System.out.println(message + " secondary RMI Server");
                        i = 1;
                    } catch (RemoteException e) {

                        try {
                            s = (RmiServer) LocateRegistry.getRegistry(host, rmi_port).lookup(rmi_name);
                        } catch (ConnectException ex) {
                            System.out.println("Main server down " + i + " already ...");
                            i++;
                            if (i == 5) {
                                System.out.println("Primary RMI down, secundary server taking over...");
                                break;
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            } catch (NotBoundException | RemoteException e) {
                //e.printStackTrace();
            }

            try {
                rmiStart();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }else
            System.out.println("Invalid Properties File");
    }

    /**
     * Função para ler as propriedades do RMI, como o host, o nome e o porto
     * @return true caso tenha lido todos os argumentos esperados, false caso contrário
     */
    public static Boolean readPropertiesFile(boolean cs){
        boolean changed = false;

        try {
            File myObj = new File("PropertiesFile.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
            	String[] data = reader.nextLine().split(": ");
                if(data[0].compareTo("host")==0) {
                    if(host == null)
                        host = data[1];
                    else if(!host.equals(data[1])) {
                        host = data[1];
                        changed = true;
                    }
                }
                if(data[0].compareTo("rmiName")==0) {
                    if(rmi_name == null)
                        rmi_name = data[1];
                    else if(!rmi_name.equals(data[1])) {
                        rmi_name = data[1];
                        changed = true;
                    }
                }
                if(data[0].compareTo("rmiPort")==0) {
                    if(rmi_port == 0)
                        rmi_port = Integer.parseInt(data[1]);
                    else if(rmi_port != Integer.parseInt(data[1])) {
                        rmi_port = Integer.parseInt(data[1]);
                        changed = true;
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        if(cs)
            return changed;
        else
            return host != null && rmi_name != null && rmi_port != 0;
    }

    /**
     * Método para iniciar o RMI.
     * Cria registo no porto 7001 e, caso consiga, o programa fica de 1 em 1 segundos a ver se
     * deixa de obter resposta (servidor vai abaixo). Se for abaixo volta a chama esta função
     * para criar um novo registo.
     * Cria e corre uma thread para verificar os estados das mesas de voto e outra para fechar
     * as eleições caso a data do sistema seja superior à data do fim da eleição.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void rmiStart() throws ClassNotFoundException, SQLException {
        try {
            rmiServer = new RmiServerImpl();
            r = LocateRegistry.createRegistry(rmi_port);
            r.bind(rmi_name, rmiServer);
            System.out.println("RMI Server ready.");
            readObjects();
            checkVTStates vtThreadRmi = new checkVTStates();
            vtThreadRmi.start();
            checkElectionEndDate eedThreadRmi = new checkElectionEndDate();
            eedThreadRmi.start();
            checkServer cs = new checkServer();
            cs.start();
        } catch (RemoteException | AlreadyBoundException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Função que informa se o servidor RMI está a correr
     * @return mensagem informativa
     * @throws RemoteException
     */
    @Override
    public String isAlive() throws RemoteException {
        return "Running";
    }

    /**
     * Função para ler os dados de ficheiros objecto
     */
    public static void readObjects() {
        try {
            FileInputStream fi;
            ObjectInputStream oi;

            fi = new FileInputStream("Users.txt");
            oi = new ObjectInputStream(fi);
            users = (ArrayList<User>) oi.readObject();
            oi.close();
            fi.close();

            fi = new FileInputStream("Elections.txt");
            oi = new ObjectInputStream(fi);
            elections = (ArrayList<Election>) oi.readObject();
            oi.close();
            fi.close();

            fi = new FileInputStream("Lists.txt");
            oi = new ObjectInputStream(fi);
            lists = (ArrayList<List>) oi.readObject();
            oi.close();
            fi.close();

            fi = new FileInputStream("Votes.txt");
            oi = new ObjectInputStream(fi);
            votes = (ArrayList<Vote>) oi.readObject();
            oi.close();
            fi.close();

            fi = new FileInputStream("VotingTables.txt");
            oi = new ObjectInputStream(fi);
            votingTables = (CopyOnWriteArrayList<VotingTable>) oi.readObject();
            oi.close();
            fi.close();

        } catch (FileNotFoundException e) {
            //System.out.println("File not found");
        } catch (IOException e) {
            //System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            //System.out.println("class not found exception");
            //e.printStackTrace();
        }
    }

    /**
     * Função para ler os dados de ficheiros objecto
     * @param type tipo de ficheiros a serem escritosa/atualizados
     */
    public void writeObject(String type) {
        String fileName = null;

        try {
            if (type.equals("users"))
                fileName = "Users.txt";
            if (type.equals("elections"))
                fileName = "Elections.txt";
            if (type.equals("lists"))
                fileName = "Lists.txt";
            if (type.equals("votes"))
                fileName = "Votes.txt";
            if (type.equals("votingTables"))
                fileName = "VotingTables.txt";

            if(fileName != null) {
                FileOutputStream fo = new FileOutputStream(fileName);
                ObjectOutputStream oo = new ObjectOutputStream(fo);

                if (type.equals("users"))
                    oo.writeObject(users);
                if (type.equals("elections"))
                    oo.writeObject(elections);
                if (type.equals("lists"))
                    oo.writeObject(lists);
                if (type.equals("votes"))
                    oo.writeObject(votes);
                if (type.equals("votingTables"))
                    oo.writeObject(votingTables);

                oo.close();
                fo.close();
            }
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            e.printStackTrace();
        }
    }

    /**
     * Função para registar um utilizador no sistema
     * @param name nome
     * @param type tipo
     * @param password password
     * @param department departamento
     * @param phoneNumber número de telemóvel
     * @param residenceAddress morad
     * @param ccNumber número do CC
     * @param ccExpirationDate validade do CC
     * @return resposta que confirma se a operação foi executada ou não
     * @throws RemoteException
     */
    @Override
    public String registerUser(String name, String role, String type, String password, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate) {

        for (User user: users) {
            if (user.getName().equals(name)) {
                return ("User already exists!");
            }
        }
        /* Modification Start */
        users.add(new User(name, null, role, type, password, department, phoneNumber, residenceAddress, ccNumber, ccExpirationDate));
        /* Modification End */
        writeObject("users");
        return ("User registered");
    }
    
    @Override
    public String registerFacebookUser(String name, String uid, String role, String type, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate) {
    	String userId = null;
        for (User user: users) { 
        	userId = user.getUid();
            if (userId != null && userId.equals(uid)) {
                return ("User already exists!");
            }
        }
        /* Modification Start */
        users.add(new User(name, uid, role, type, null, department, phoneNumber, residenceAddress, ccNumber, ccExpirationDate));
        /* Modification End */
        writeObject("users");
        return ("User registered");
    }

    /**
     * Busca um user através do seu ID
     * @param uid
     * @return
     * @throws RemoteException
     */
    @Override
    public User getValidUser(String uid) throws RemoteException {
    	User objUser = null;
    	String userId = null;
    	
    	for(User user : users) {
    		userId = user.getUid();
    		if(userId != null && userId.equalsIgnoreCase(uid))
    			objUser = user;
    		else
    			objUser = null;
    	}
    	return objUser;
    }

    /**
     * Associa um utilizador a uma conta de facebook ao guardar neste o ID de uma conta
     * @param uId
     * @param username
     * @param role
     * @param type
     * @param department
     * @return
     * @throws RemoteException
     */
    @Override
    public String linkFacebookUId(String uId, String username, String role, String type, String department) throws RemoteException {
    	int index = 0;
    	User objUser = null;
    	
    	for(User user : users) {
    		if(user.getName().equalsIgnoreCase(username) && user.getRole().equalsIgnoreCase(role) 
    				&& user.getType().equalsIgnoreCase(type) && user.getDepartment().equalsIgnoreCase(department)) {
    			objUser = user;
    			break;
    	} else
    			index++;
    	}
    	
    	if(objUser != null) {
    		objUser.setUid(uId);
    		users.set(index, objUser);
    		writeObject("users");
    	}
    	return "User is linked with facebook";
    }
    
    /**
     * Função para criar uma eleição
     * @param title titulo
     * @param description descrição
     * @param startDate data de início
     * @param endDate data de fim
     * @param startHour hora de início
     * @param startMinute minuto de início
     * @param endHour hora de fim
     * @param endMinute minuto de fim
     * @param type tipo
     * @param lists listas
     * @return resposta que confirma se a operação foi executada ou não
     * @throws RemoteException
     */
    @Override
    public String createElection(String title, String description, String startDate, String endDate, String startHour, String startMinute, String endHour, String endMinute, String type, ArrayList<List> lists) throws RemoteException {
        
    	elections.add(new Election(title, description, startDate, endDate, startHour, startMinute, endHour, endMinute, type, lists));
        writeObject("elections");
        return ("Election created");
    }

    /**
     * Função para adicionar ou remover mesas de voto associadas a uma dada eleição
     * @param votingTable mesa de voto escolhida
     * @param election eleição escolhida
     * @param action ação a ser executada
     * @return resposta que confirma se a operação foi executada ou não
     * @throws RemoteException
     */
    @Override
    public String manageVotingTables(VotingTable votingTable, Election election, String action) throws RemoteException {
        boolean contains = false;
        boolean foundVotingTable = false;
        int index = 0;
        VotingTable vt = null;
        ArrayList<Election> vtElections;
        System.out.println("votingTable size : "+votingTables.size());
        Iterator<VotingTable> failSafeIterator = votingTables.iterator();
        while(failSafeIterator.hasNext()) {
        	System.out.println("votingTable hasNext ");
        	vt = failSafeIterator.next();
        	System.out.println("vt.department "+vt.getDepartment());
        	if(vt.getDepartment().equals(votingTable.getDepartment())){
        		foundVotingTable = true;
                break;
            }
            index++;
        }
        /*for(VotingTable vt: votingTables){
            if(vt.getDepartment().equals(votingTable.getDepartment())){
                break;
            }
            index++;
        }*/
        System.out.println("index : "+index+" *** foundVotingTable : "+foundVotingTable);
        if (action.equals("add")) {
        	if(foundVotingTable) {
	            vtElections = votingTable.getElections();
	            if(vtElections != null) {
	                for (Election e : vtElections) {
	                    if (e.getTitle().equals(election.getTitle())) {
	                        contains = true;
	                        break;
	                    }
	                }
	            }
	            System.out.println("contains : "+contains);
	            if (contains)
	                return ("Election was already added to voting table");
	            else 
	                votingTables.get(index).addElection(election);
	                
                writeObject("votingTables");
                return ("Election added to voting table");
        	} else {	//To add new voting table in the list
        		System.out.println("To add new voting table");
        		votingTable.addElection(election);
        		votingTables.add(votingTable);
        		
        		writeObject("votingTables");
                return ("New voting table is added");
        	}
        }
        if (action.equals("remove")) {
            votingTables.get(index).removeElection(election);
            writeObject("votingTables");
            return ("Election removed from voting table");
        }

        return ("Invalid action");
    }

    /**
     * Função para criar uma lista
     * @param name nome
     * @param type tipo
     * @param users membros
     * @return resposta que confirma se a operação foi executada ou não
     * @throws RemoteException
     */
    @Override
    public String createList(String name, String type, ArrayList<User> users) throws RemoteException {
        for (List list: lists) {
            if (list.getName().equals(name))
                return ("Name already taken");
        }

        lists.add(new List(name, type, users));
        writeObject("lists");
        return ("List created");
    }

    /**
     * Função para editar um parametro numa eleição
     * @param election eleição a editar
     * @param toEdit valor para editar
     * @param newValue valor novo
     * @return resposta que confirma se a operação foi executada ou não
     * @throws RemoteException
     */
    @Override
    public String editElection(Election election, int toEdit, String newValue) throws RemoteException {
        for (Election e: elections) {
            if (election.getTitle().equals(e.getTitle())) {

                if (toEdit == 1)
                    e.setTitle(newValue);
                if (toEdit == 2)
                    e.setDescription(newValue);
                if (toEdit == 3)
                    e.setStartDate(newValue);
                if (toEdit == 4)
                    e.setEndDate(newValue);
                if (toEdit == 5)
                    e.setStartHour(newValue);
                if (toEdit == 6)
                    e.setStartMinute(newValue);
                if (toEdit == 7)
                    e.setEndHour(newValue);
                if (toEdit == 8)
                    e.setEndMinute(newValue);
                if (toEdit == 9)
                    e.setType(newValue);

                writeObject("elections");
                return ("Election properties changed");
            }
        }
        return ("Elections unchanged");
    }

    /**
     * Função para obter as mesas de voto
     * @return mesas de voto
     * @throws RemoteException
     */
    public CopyOnWriteArrayList<VotingTable> getVotingTables() throws RemoteException {
        return votingTables;
    }

    /**
     * Função para obter os votos
     * @return votos
     * @throws RemoteException
     */
    @Override
    public ArrayList<Vote> getVotes() throws RemoteException {
        return votes;
    }

    /**
     * Função para obter as eleições
     * @return eleições
     * @throws RemoteException
     */
    @Override
    public ArrayList<Election> getElections() throws RemoteException {
        return elections;
    }

    /**
     * Função para obter os utilizadores
     * @return utilizadores
     * @throws RemoteException
     */
    @Override
    public ArrayList<User> getUsers() throws RemoteException {
        return users;
    }

    /**
     * Função que adiciona um voto à lista de votos
     * @param vote voto a adiconar
     * @throws RemoteException
     */
    @Override
    public void addVote(Vote vote) throws RemoteException {
    	/* Modification Start */
    	int noOfVotes = 0;
    	/* Modification End */
    	
        for (Election e: elections) {
            if (e.getTitle().equals(vote.getElection().getTitle())) {
                if (e.getState()) {
                    votes.add(vote);
                    writeObject("votes");
                    /* Modification Start */
                    System.out.println("While vote, election "+e.getTitle());
                    System.out.println("map contains object : "+noOfVotesInElection.containsKey(e.getTitle()));
                    if(noOfVotesInElection.containsKey(e.getTitle())) {
                		noOfVotes = noOfVotesInElection.get(e.getTitle());
                		System.out.println("noOfVotes : "+noOfVotes);
                		noOfVotes++;
                		noOfVotesInElection.replace(e.getTitle(), noOfVotes);
                    } else {
                    	noOfVotesInElection.put(e.getTitle(), 1);
                    }
                    /* Modification End */
                }
            }
        }
    }
    /* Modification Start */
    @Override
    public Map<String, Integer> getNoOfUsersVoted() throws RemoteException {
    	return noOfVotesInElection;
    }
    
    @Override
    public String getConnectedUsers() throws RemoteException {
    	StringBuilder strbuild = new StringBuilder();
		for(String user : loggedInUsers.keySet()) {
			strbuild.append(user).append(",");
		}
		if(strbuild.length() > 1)
			strbuild.deleteCharAt(strbuild.length() - 1);
		
    	return strbuild.toString();
    }
    
    @Override
    public void logout(String username) throws RemoteException {
    	if(loggedInUsers.containsKey(username))
    		loggedInUsers.remove(username);
    }
    /* Modification End */
    /**
     * Função que pesquisa por uma lista candidata utilizando o nome introduzido
     * @param listName nome da lista a ser pesquisada
     * @return a lista correspondente ou null caso não exista
     * @throws RemoteException
     */
    @Override
    public List getList(String listName) throws RemoteException {
        for (List l: lists) {
            if (l.getName().equals(listName)) {
                return l;
            }
        }
        return null;
    }

    /**
     * Função que verifica se um utilizador existe e se a password introduzida está correta
     * @param name nome do utilizador
     * @param password password do utilizador
     * @return true(se os dados coincidirem com o utilizador registado no sistema) ou false(se não coincidirem)
     * @throws RemoteException
     */
    @Override
    public Boolean authenticateUser(String name, String password, String role) throws RemoteException {
        for (User u: users) {
            if (u.getName().equals(name)) {
            	/* Modification Start */
                if (u.getPassword().equals(password) && u.getRole().equalsIgnoreCase(role)) {
                	loggedInUsers.put(name, true);
                	/* Modification End */
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Função para atualizar o estado e dados das mesas de voto ligadas ao servidor RMI
     * @param votingTable mesa de voto a ser atualizada
     * @param votingTerminals lista dos terminais de voto ligados à mesa de voto a ser atualizada
     * @throws RemoteException
     */
    @Override
    public void updateVotingTableState(VotingTable votingTable, CopyOnWriteArrayList<VotingTerminal> votingTerminals) throws RemoteException {
        int index = 0;

        if (votingTables.size() != 0) {
            for (int i = 0; i < votingTables.size(); i++) {
                if (votingTables.get(i).getDepartment().equals(votingTable.getDepartment())) {
                    index = i;
                }
            }

            if (votingTables.get(index).votingTerminals != votingTerminals) {
                votingTables.get(index).setVotingTerminals(votingTerminals);
                votingTables.get(index).setLastUpdateTime(System.currentTimeMillis() / 1000);
                writeObject("votingTables");
            } else {
                votingTables.add(votingTable);
                writeObject("votingTables");
            }
        } else {
            votingTables.add(votingTable);
        }
    }

    @Override
    public ArrayList<List> getLists() {
        return lists;
    }

    /**
     * Função para filtrar e devolver apenas as listas em que um dado utilizador pode votar.
     * Faz isto primeiro ao pesquisar as listas associadas á mesa de voto em que o utilizador se encontra,
     * retirar a essas as eleições em que o utilizador já votou, e depois retirar as eleições que não correspondem
     * ao mesmo tipo do utilizador;
     * @param name nome do utilizador
     * @param department nome do departamento
     * @return a lista de elições em que o tuilizador pode votar
     * @throws RemoteException
     */
    @Override
    public ArrayList<Election> selectElections(String name, String department) throws RemoteException {
        ArrayList<Election> electionsWithoutVote = new ArrayList<>();
        ArrayList<Election> electionsType = new ArrayList<>();
        ArrayList<Election> electionsActive = new ArrayList<>();
        User userTemp = null;
        boolean found;

        if (elections.size() == 0) {
            return null;
        }

        for (User u : users) {
            if (u.getName().equals(name)) {
                userTemp = u;
            }
        }

        if (userTemp != null){
            if (userTemp.elections.size() == 0) {
                return elections;
            } else {
                for (Election e: elections) { //filtrar pelas eleições em comum
                    found = false;
                    for (int j = 0; j < userTemp.elections.size(); j++) {
                        if (e.getTitle().equals(userTemp.elections.get(j).getTitle())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        electionsWithoutVote.add(e);
                    }
                }
            }
        }

        for (Election e: electionsWithoutVote) {
            if (e.getType().equals(userTemp.getType())) {
                electionsType.add(e);
            }
        }

        for(Election e: electionsType){
            if(e.getState())
                electionsActive.add(e);
        }

        return electionsActive;
    }

    /**
     * Função para se obter uma elição pesquisada pelo seu nome
     * @param electionName nome da eleição a ser pesquisada
     * @return a eleição cujo nome corresponde ao nome procurado, ou null se não for encontrada nenhuma
     * @throws RemoteException
     */
    @Override
    public Election getSpecificElection(String electionName) throws RemoteException {
        for (Election e: elections) {
            if (e.getTitle().equals(electionName)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Função para se adicionar uma eleição à lista de eleições em que o utilizador já votou
     * @param userName nome do utilizador ao qual vai ser acrescentada a eleição
     * @param election eleição a ser registada como já tendo sido votada pelo utilizador
     * @throws RemoteException
     */
    @Override
    public void addUserElection(String userName, Election election) throws RemoteException {
        for (User u: users) {
            if (u.getName().equals(userName)) {
                u.addElection(election);
                writeObject("users");
            }
        }
    }

    /**
     * Função usada para verificar a existência de uma mesa de voto pertencente a um departamento
     * @param name nome do departamento que se quer verificar a existência
     * @return true(caso o departamento já esteja com uma mesa de voto ativa) ou false(caso nao tenha uma mesa de voto ativa)
     * @throws RemoteException
     */
    @Override
    public boolean checkVotingTable(String name) throws RemoteException {
        for (VotingTable vt: votingTables) {
            if (vt.getDepartment().equals(name))
                return true;
        }
        return false;
    }

    /**
     * Função usada para verificar se algum departamento já utiliza um endereço multicast
     * @param address endereço multicast da mesa de voto
     * @return true(caso já exista um departamento a usar o endereço multicast) ou false(caso não haja)
     * @throws RemoteException
     */
    @Override
    public boolean checkVotingTableAddress(String address) throws RemoteException {
        for (VotingTable vt: votingTables) {
            if (vt.getAddress().equals(address))
                return true;
        }
        return false;
    }

    /**
     * Thread que verifica os estados das mesas de voto.
     * Cada mesa de voto tem uma variavel que informa a ultima vez que
     * foi atualizada, e as mesas de voto são atualizadas de 1 em 1s.
     * Caso a mesa de voto não tenha sido atualizada à mais de 5s então
     * é considerada como inativa e removida da listas das mesas de voto.
     */
    static class checkVTStates extends Thread {
        Thread thread;

        checkVTStates() {
            thread = new Thread(this);
            thread.start();
        }

        public void run() {
            long currentTime;

            while (true) {
                for (VotingTable vt: votingTables) {
                    currentTime = System.currentTimeMillis() / 1000;
                    if (currentTime - vt.getLastUpdateTime() > 5)
                        votingTables.remove(vt);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    /**
     * Thread que verifica se a data do sistema é superior à data do fim da eleições
     * guardadas e, se isso se verificar, o estado da eleição fica a false, o que
     * equivale a deixar de estar ativa.
     */
    static class checkElectionEndDate extends Thread {
        Thread thread;

        checkElectionEndDate() {
            thread = new Thread(this);
            thread.start();
        }

        public void run() {
            Date endDate = null;
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            while (true) {
                for (Election e: elections) {
                    try {
                        endDate = sdf.parse(e.getEndDate());
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }

                    if (endDate != null) {
                        if (currentDate.after(endDate)) {
                            e.setState(false);
                        } else if (currentDate == endDate) {
                            if (Integer.parseInt(e.getEndHour()) > LocalTime.now().getHour())
                                e.setState(false);
                            else if (Integer.parseInt(e.getEndHour()) == LocalTime.now().getHour()) {
                                if (Integer.parseInt(e.getEndMinute()) > LocalTime.now().getMinute())
                                    e.setState(false);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    /**
     * Thread que verifica de 2 em 2 segundos se as informações do RMI mudaram e volta a configurar
     */
    static class checkServer extends Thread {
        Thread thread;

        checkServer() {
            thread = new Thread(this);
            thread.start();
        }

        public void run() {
            while (true) {
                if(readPropertiesFile(true)) {
                    System.out.println("RMI Server changed properties");
                    try {
                        r.unbind(rmi_name);
                        r = LocateRegistry.createRegistry(rmi_port);
                        r.bind(rmi_name, rmiServer);
                    } catch (RemoteException ex) {
                        //ex.printStackTrace();
                    } catch (NotBoundException e) {
                        //e.printStackTrace();
                    } catch (AlreadyBoundException e) {
                        //e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }
}