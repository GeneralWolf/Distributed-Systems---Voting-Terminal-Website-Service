package com.evote.models;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class VotingTable implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String department;
    protected ArrayList<Election> elections;
    public CopyOnWriteArrayList<VotingTerminal> votingTerminals;
    protected long lastUpdateTime;
    protected boolean state;
    protected String address;

    public VotingTable(String department, Boolean state, String address){
        this.department = department;
        this.elections = new ArrayList<>();
        this.votingTerminals = new CopyOnWriteArrayList<>();
        this.lastUpdateTime = 0;
        this.state = state;
        this.address = address;
    }

    public String getDepartment(){ return department; }
    public ArrayList<Election> getElections(){ return elections; }
    public CopyOnWriteArrayList<VotingTerminal> getVotingTerminals(){ return votingTerminals; }
    public long getLastUpdateTime(){ return lastUpdateTime; }
    public boolean getState() {
        return state;
    }
    public String getAddress(){
        return address;
    }

    public void setDepartment(String department){ this.department = department; }
    public void setVotingTerminals(CopyOnWriteArrayList<VotingTerminal> votingTerminals){ this.votingTerminals = votingTerminals; }
    public void setLastUpdateTime(long lastUpdateTime){ this.lastUpdateTime = lastUpdateTime; }

    public void addElection(Election election){ elections.add(election); }
    public void removeElection(Election election){
        for(int i = 0; i < elections.size(); i++){
            if(elections.get(i).getTitle().equals(election.getTitle()))
                elections.remove(i);
        }
    }
}
