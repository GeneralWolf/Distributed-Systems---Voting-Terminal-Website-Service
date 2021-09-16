package com.evote.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Election implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String title;
    protected String description;
    protected String startDate;
    protected String endDate;
    protected String startHour;
    public String startMinute;
    protected String endHour;
    protected String endMinute;
    protected String type;
    protected boolean state;
    protected ArrayList<List> lists;
    public String candidateList;
    protected String status;
    protected String wonBy;
    protected String percentWinVotes;
    protected String totalVotes;
    protected ArrayList<VotingTable> lstVotingTables;
    
    /*public Election(String title, String description, String startDate, String endDate, String startHour, String startMinute, String endHour, String endMinute, String type, ArrayList<List> lists) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.type = type;
        this.state = true;
        this.lists = lists;
    }*/

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getStartHour() {
        return startHour;
    }
    public String getStartMinute() {
        return startMinute;
    }
    public String getEndHour() {
        return endHour;
    }
    public String getEndMinute() {
        return endMinute;
    }
    public String getType() {
        return type;
    }
    public boolean getState() {
        return state;
    }
    public ArrayList<List> getLists() {
        return lists;
    }
     
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }
    public void setStartMinute(String startMinute) {
        this.startMinute = startMinute;
    }
    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }
    public void setEndMinute(String endMinute) {
        this.endMinute = endMinute;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setState(boolean state) {
        this.state = state;
    }
	public String getCandidateList() {
		return candidateList;
	}
	public void setCandidateList(String candidateList) {
		this.candidateList = candidateList;
	}
	public void setLists(ArrayList<List> lists) {
		this.lists = lists;
	}
	public String getStatus() {
		return status;
	}
	public String getWonBy() {
		return wonBy;
	}
	public String getPercentWinVotes() {
		return percentWinVotes;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setWonBy(String wonBy) {
		this.wonBy = wonBy;
	}
	public void setPercentWinVotes(String percentWinVotes) {
		this.percentWinVotes = percentWinVotes;
	}
	public String getTotalVotes() {
		return totalVotes;
	}
	public void setTotalVotes(String totalVotes) {
		this.totalVotes = totalVotes;
	}
	public ArrayList<VotingTable> getLstVotingTables() {
		return lstVotingTables;
	}
	public void setLstVotingTables(ArrayList<VotingTable> lstVotingTables) {
		this.lstVotingTables = lstVotingTables;
	}

}