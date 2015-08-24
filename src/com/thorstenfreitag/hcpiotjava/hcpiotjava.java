package com.thorstenfreitag.hcpiotjava;

import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;  
import java.util.HashSet;  
import java.util.Set;  

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import javax.sql.DataSource;
import javax.websocket.OnMessage;  
import javax.websocket.server.ServerEndpoint;  

import javax.websocket.OnClose;    
import javax.websocket.OnOpen;  
import javax.websocket.Session;  

import com.google.gson.Gson;
import com.thorstenfreitag.hcpiotjava.Measurement;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sap.security.core.server.csi.IXSSEncoder;
import com.sap.security.core.server.csi.XSSEncoder;

@ServerEndpoint("/websocket")  
public class hcpiotjava {
	
	private DataSource ds;
	private EntityManagerFactory emf;
	
	
	 private static Set<Session> clients = Collections
			 .synchronizedSet(new HashSet<Session>());  
	 
	 private static Set<Session> subscribedClients = Collections
			 .synchronizedSet(new HashSet<Session>());
	
@OnOpen
	public void init(Session session)  {
	
	//We are doing this now only on SUBSCRIBE
	clients.add(session); 
	
		try {
			InitialContext ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");

			Map properties = new HashMap();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
			emf = Persistence.createEntityManagerFactory("hcpiotjava", properties);
		} catch (NamingException e) {
			
		}
	}
	
	@OnMessage public String echo(String msg, Session session){  
	
	//The client has to send the message SUBSCRIBE to receive a copy of every event.	
		if (msg.equals("SUBSCRIBE")) {
				
			subscribedClients.add(session);

//	           try {  
//	       
//	            	clients.add(session);   
//	            } catch (Exception e) {  
//	                    // ignore for now  
//	            } 
	            
	            
			
			return "SUBSCRIBED";
			
		} else {
		DataHelper dataHelper = new DataHelper(emf);
		
		Measurement measurement = extractMeasurementData(msg);
		dataHelper.addMeasurement(measurement);
		
//We are only iterating through the subscribed clients
		for (Session client : subscribedClients) {  
            try {  
            	client.getBasicRemote().sendText(msg);   
            } catch (Exception e) {  
                    // ignore for now  
            }  
		}
		
		return "INSERTED: "+msg;  
		}
         

	}
	

@OnClose
	public void destroy(Session session) {
		//Disconnect Client
		clients.remove(session);
		
		subscribedClients.remove(session);
		
		//Persist open messages
		emf.close();
		 
	}


public void broadcastAll(String msg){  
//This allows to send a message to all clients, even if they haven't send the SUBSCRIBE message. Not used yet...	
	for (Session client : clients) {  
        try {  
        	client.getBasicRemote().sendText(msg);   
        } catch (Exception e) {  
                // ignore for now  
        }  
	}
   

}

	/*
	 * Extracts a Measurement object (sensor values) out of the parameters
	 * provided in the HTTP-request
	 * 
	 * @param request The HTTP-request object
	 * 
	 * @return The derived Measurement object
	 */
	private Measurement extractMeasurementData(String request) {
		//Measurement measurement = new Measurement();
		
		Gson gson = new Gson();

		//System.out.println(
		//    gson.fromJson("{'id':1,'unit':'meters','sensorValue':'15.0','sensor':1}", Measurement.class));
		
		//Important: in Gson, the names of variables must be same and even the dataType should be correct otherwise it creates problems
		Measurement measurement = gson.fromJson(request, Measurement.class);

		measurement.setStoredAt(new Timestamp(new Date().getTime()));
		
		//DEBUG stuff...
		//System.out.println(gson.fromJson(request, Measurement.class));		
		//System.out.println(measurement.getValue().toString());
		
		return measurement;
	}
	
	/*
	 * Encodes a text to avoid cross-site-scripting vulnerability
	 * 
	 * @param request The text to be encoded
	 * 
	 * @return The encoded String
	 */
	private String encodeText(String text) {

		String result = null;
		if (text != null && text.length() > 0) {
			IXSSEncoder xssEncoder = XSSEncoder.getInstance();

			try {
				result = (String) xssEncoder.encodeURL(text).toString();
				result = URLDecoder.decode(result, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return result;
	}
	
	
	
}