package com.evote.models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name;
	protected String uid;
    protected String role;
    protected String type;
    protected String password;
    protected String department;
    protected long phoneNumber;
    protected String residenceAddress;
    protected long ccNumber;
    protected String ccExpirationDate;
    protected ArrayList<Election> elections;
    /*
    public User(String name, String type, String password, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate){
        this.name = name;
        this.type = type;
        this.password = password;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.residenceAddress = residenceAddress;
        this.ccNumber = ccNumber;
        this.ccExpirationDate = ccExpirationDate;
        this.elections = new ArrayList<>();
    }*/

    public String getName() { return name; }
    public String getType() { return type; }
    public String getPassword() { return password; }
    public String getDepartment() { return department; }
    public long getPhoneNumber() { return phoneNumber; }
    public String getResidenceAddress() { return residenceAddress; }
    public long getCCNumber() { return ccNumber; }
    public String getCCExpirationDate() { return ccExpirationDate; }
    public void addElection(Election election){
        elections.add(election);
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setPhoneNumber(long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setResidenceAddress(String residenceAddress) {
		this.residenceAddress = residenceAddress;
	}

	public void setCcNumber(long ccNumber) {
		this.ccNumber = ccNumber;
	}

	public void setCcExpirationDate(String ccExpirationDate) {
		this.ccExpirationDate = ccExpirationDate;
	}

//	public void setElections(ArrayList<Election> elections) {
//		this.elections = elections;
//	}
	public ArrayList<Election> getElections() {
		return elections;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}