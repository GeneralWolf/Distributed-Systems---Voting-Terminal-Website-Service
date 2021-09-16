package com.evote.models;

import java.io.Serializable;
import java.util.ArrayList;

public class List implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name;
    protected String type;
    protected ArrayList<User> users;

    public List(String name, String type, ArrayList<User> users) {
        this.name = name;
        this.type = type;
        this.users = users;
    }

    public String getName() { return name; }
    public String getType() {
        return type;
    }
    public ArrayList<User> getUsers() {
        return users;
    }

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}
}