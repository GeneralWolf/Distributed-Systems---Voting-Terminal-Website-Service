package com.evote.remote;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.evote.models.Election;
import com.evote.models.List;
import com.evote.models.User;
import com.evote.models.Vote;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;

public interface RmiServer extends Remote {
    String isAlive() throws RemoteException;
    /* Modification Start */
    String registerUser(String name, String role, String type, String password, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate) throws RemoteException;
    /* Modification End */
    String createElection(String title, String description, String startDate, String endDate, String startHour, String startMinute, String endHour, String endMinute, String type, ArrayList<List> lists) throws RemoteException;
    String createList(String name, String type, ArrayList<User> users) throws RemoteException;
    String editElection(Election election, int toEdit, String newValue) throws RemoteException;
    ArrayList<Vote> getVotes() throws RemoteException;
    ArrayList<Election> getElections() throws RemoteException;
    ArrayList<User> getUsers() throws RemoteException;
    void addVote(Vote vote) throws RemoteException;
    List getList(String listName) throws RemoteException;
    /* Modification Start */
    Boolean authenticateUser(String name, String password, String role) throws RemoteException;
    /* Modification End */
    String manageVotingTables(VotingTable votingTable, Election elections, String action) throws RemoteException;
    CopyOnWriteArrayList<VotingTable> getVotingTables() throws RemoteException;
    void updateVotingTableState(VotingTable votingTable, CopyOnWriteArrayList<VotingTerminal> votingTerminals) throws RemoteException;
    ArrayList<List> getLists() throws RemoteException;
    ArrayList<Election> selectElections(String name, String department) throws RemoteException;
    Election getSpecificElection(String electionName) throws RemoteException;
    void addUserElection(String userName, Election election) throws RemoteException;
    boolean checkVotingTable(String name) throws RemoteException;
    boolean checkVotingTableAddress(String address) throws RemoteException;
    /* Modification Start */
    Map<String, Integer> getNoOfUsersVoted() throws RemoteException;
    String getConnectedUsers() throws RemoteException;
    void logout(String username) throws RemoteException;
    String registerFacebookUser(String name, String uid, String role, String type, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate) throws RemoteException;
    User getValidUser(String uid) throws RemoteException;
    String linkFacebookUId(String uId, String username, String role, String type, String department) throws RemoteException;
    /* Modification End */
}
