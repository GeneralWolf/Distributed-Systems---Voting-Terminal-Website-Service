package com.evote.controllers;

import java.rmi.RemoteException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.evote.remote.RmiClient;
import com.evote.remote.RmiServer;

@Component
public class StartUpExecute {
	@Autowired
	private RmiClient rmiClient;
	public static RmiServer rmiServer;
	
  @EventListener(ContextRefreshedEvent.class)
  public void contextRefreshedEvent() {								//cria um utilizador admin base
	  String reply = null;
    System.out.println("**** Inside context refreshed event ****");
    try {
	    rmiServer = rmiClient.getRmi();
		if(rmiServer != null && ("Running".equalsIgnoreCase(rmiServer.isAlive()))) {
			reply = rmiServer.registerUser("Administrator1", "Admin", "Staff", "secretadmin", "LV8", 0, null, 0, null);	
		} 
		System.out.println("reply : "+reply);
    } catch (RemoteException e) {
		e.printStackTrace();
	}
  }
}