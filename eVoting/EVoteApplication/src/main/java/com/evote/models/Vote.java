package com.evote.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Vote implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Election election;
    protected List list;
    protected String department;
    protected LocalDateTime moment;
    public boolean nullVote;
    public boolean whiteVote;

    public Vote(Election election, List list, String department, LocalDateTime moment, boolean nullVote, boolean whiteVote){
        this.election = election;
        this.list = list;
        this.department = department;
        this.moment = moment;
        this.nullVote = nullVote;
        this.whiteVote = whiteVote;
    }

    public Election getElection(){
        return election;
    }
    public List getList(){
        return list;
    }
    public String getDepartment(){
        return department;
    }
    public LocalDateTime getMoment(){
        return moment;
    }
    public boolean getNullVote(){
        return nullVote;
    }
    public boolean getWhiteVote(){
        return whiteVote;
    }

    public void setList(List list){
        this.list = list;
    }
    public void setAddress(String department){
        this.department = department;
    }
    public void setMoment(LocalDateTime moment){
        this.moment = moment;
    }
}
