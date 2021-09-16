package com.evote.models;
import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name;
    /* Modification Start */
	protected String uid;
    protected String role;
    /* Modification End */
    protected String type;
    protected String password;
    protected String department;
    protected long phoneNumber;
    protected String residenceAddress;
    protected long ccNumber;
    protected String ccExpirationDate;
    public ArrayList<Election> elections;

    public User(String name, String uid, String role, String type, String password, String department, long phoneNumber, String residenceAddress, long ccNumber, String ccExpirationDate){
        this.name = name;
        /* Modification Start */
        this.uid = uid;
        this.role = role;
        /* Modification Start */
        this.type = type;
        this.password = password;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.residenceAddress = residenceAddress;
        this.ccNumber = ccNumber;
        this.ccExpirationDate = ccExpirationDate;
        this.elections = new ArrayList<>();
    }

    public String getName() { return name; }
    /* Modification Start */
    public String getRole() { return role; }
    /* Modification End */
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

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}