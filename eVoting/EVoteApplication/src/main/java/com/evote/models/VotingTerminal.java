package com.evote.models;

import java.io.Serializable;

public class VotingTerminal implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int id;
    protected boolean state;

    public VotingTerminal(int id, Boolean state){
        this.id = id;
        this.state = state;
    }

    public int getID(){ return id; }
    public boolean getState(){ return state; }

    public void setState(Boolean state){ this.state = state; }
}