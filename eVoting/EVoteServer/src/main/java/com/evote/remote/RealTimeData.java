package com.evote.remote;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.evote.models.Election;
import com.evote.models.User;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class RealTimeData extends WebSocketServer {
	private static String host;
    private static String rmi_name;
    private static int rmi_port;
    static RmiServer rmi;
    private static int websocket_port;
    
  public RealTimeData(int port) throws UnknownHostException {
    super(new InetSocketAddress(port));
  }

  public RealTimeData(InetSocketAddress address) {
    super(address);
  }

  public RealTimeData(int port, Draft_6455 draft) {
    super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    conn.send("Welcome to the server!"); //This method sends a message to the new client
//    broadcast("new connection: " + handshake
//        .getResourceDescriptor()); //This method sends a message to all clients connected
    System.out.println(
        conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    broadcast(conn + " has left the room!");
    System.out.println(conn + " has left the room!");
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    broadcast(message);
    System.out.println(conn + ": " + message);
    
  }

  @Override
  public void onMessage(WebSocket conn, ByteBuffer message) {
    broadcast(message.array());
    System.out.println(conn + ": " + message);
  }


  public static void main(String[] args) throws InterruptedException, IOException {
    boolean isReadProperties = readPropertiesFile();
	  RealTimeData s = new RealTimeData(websocket_port);
    s.start();
    System.out.println("ChatServer started on port: " + s.getPort());

    boolean exit = false;
    while (!exit) {
        if(isReadProperties) {
            try {
                rmi = (RmiServer) LocateRegistry.getRegistry(host, rmi_port).lookup(rmi_name);
                exit = true;
            } catch (ConnectException ex) {
                System.out.println("RMI Server is not ready yet");
            } catch (NotBoundException | RemoteException ex) {
                //ex.printStackTrace();
            }
        }
    }
    CopyOnWriteArrayList<VotingTable> lstVotingTables = null;
    JSONObject jsonToSend = null;
    JSONObject tempJson = null;
    JSONObject tempJson1 = null;
    JSONArray jsonArr = null;
    JSONArray tempJsonArr = null;
    int noOfVotes = 0;
    String usernames = null;
//    Map<String, Integer> votedElections = null;
//    Set<String> keys = null;
    
    try {
	    while(true) {
			jsonToSend = new JSONObject();
			jsonArr = new JSONArray();
			
			lstVotingTables = rmi.getVotingTables();
			System.out.println("list of voting tables "+lstVotingTables);
			for(VotingTable votTable : lstVotingTables) {
				tempJson = new JSONObject();
				tempJson.put("department", votTable.getDepartment());
				tempJson.put("state", votTable.getState());
				tempJsonArr = new JSONArray();
				for(VotingTerminal votTerm : votTable.getVotingTerminals()) {
					tempJson1 = new JSONObject();
					tempJson1.put("terminalId", votTerm.getID());
					tempJson1.put("terminalState", votTerm.getState());
					
					tempJsonArr.add(tempJson1);
				}
				tempJson.put("votingTerminals", tempJsonArr);
				jsonArr.add(tempJson);
			}
			jsonToSend.put("VotingTables", jsonArr);
			
			/*votedElections = rmi.getNoOfUsersVoted();
			tempJsonArr = new JSONArray();
			if(votedElections != null && !votedElections.isEmpty()) {
				keys = votedElections.keySet();
				tempJson = new JSONObject();
				for(String election : keys) {
					tempJson.put(election, votedElections.get(election));
				}
				tempJsonArr.add(tempJson);
			}
			jsonToSend.put("UsersVotedElections", tempJsonArr);*/
			
			tempJson = new JSONObject();
			tempJsonArr = new JSONArray();
			ArrayList<User> lstUser = rmi.getUsers();
			for(User objUser : lstUser) {
				for(Election objElection : objUser.elections) {
					if(tempJson.containsKey(objElection.getTitle())) {
						usernames = (String) tempJson.get(objElection.getTitle());
						usernames = usernames + "," + objUser.getName();
						tempJson.replace(objElection.getTitle(), usernames);
					} else {
						tempJson.put(objElection.getTitle(), objUser.getName());
					}
				}
			}
			tempJsonArr.add(tempJson);
			jsonToSend.put("UsersVotedElections", tempJsonArr);
			
			String connectedUsers = rmi.getConnectedUsers();
			jsonToSend.put("ConnectedUsers", connectedUsers);
			
			System.out.println("jsonToSend : "+jsonToSend.toJSONString());
			s.broadcast(jsonToSend.toJSONString());
			Thread.sleep(10000);
		}
    } catch(Exception e) {
    	e.printStackTrace();
    }
    /*BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      String in = sysin.readLine();
      s.broadcast(in);
      if (in.equals("exit")) {
        s.stop(1000);
        break;
      }
    }*/
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
    if (conn != null) {
      // some errors like port binding failed may not be assignable to a specific websocket
    }
  }

  @Override
  public void onStart() {
    System.out.println("Server started!");
    setConnectionLostTimeout(0);
    setConnectionLostTimeout(100);
  }


  public static Boolean readPropertiesFile(){
      boolean h = false, rn = false, rp = false, wp = false;

      try {
          File myObj = new File("PropertiesFile.txt");
          Scanner reader = new Scanner(myObj);
          while (reader.hasNextLine()) {
              String[] data = reader.nextLine().split(": ");
              if(data[0].compareTo("host")==0) {
                  host = data[1];
                  h = true;
              }
              if(data[0].compareTo("rmiName")==0) {
                  rmi_name = data[1];
                  rn = true;
              }
              if(data[0].compareTo("rmiPort")==0) {
                  rmi_port = Integer.parseInt(data[1]);
                  rp = true;
              }
              if(data[0].compareTo("websocketServerPort")==0) {
                  websocket_port = Integer.parseInt(data[1]);
                  wp = true;
              }
          }
          reader.close();
      } catch (FileNotFoundException e) {
          //e.printStackTrace();
      }

      return h && rn && rp && wp;
  }
  
}
