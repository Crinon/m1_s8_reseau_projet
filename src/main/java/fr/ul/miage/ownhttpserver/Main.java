package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

public class Main {
	

	public static void main(String[] args) throws IOException {
		
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream inputStream = loader.getResourceAsStream("properties");
	    prop.load(inputStream);
	    Enumeration<String> enums = (Enumeration<String>) prop.propertyNames();
	    while (enums.hasMoreElements()) {
	      String key = enums.nextElement();
	      String value = prop.getProperty(key);
	      System.out.println(key + " : " + value);
	    }
	    
	    
	    
	    
	    
	    
	    System.out.println(prop.getProperty("miniweb.miage"));
		String resourceMiniweb = prop.getProperty("miniweb.miage");
		System.out.println(prop.getProperty("sites"));
		 String[] sites =prop.getProperty("sites").split(",");
		 System.out.println(sites.toString());
		int port = Integer.parseInt(prop.getProperty("port"));
        while(true) {

		// Pour chaque ligne de configuration de site
        for (int i = 0; i < sites.length; i++) {
            String nomSite = sites[i];
            System.out.println("Démarrage du site " +nomSite);
    		InetAddress host = InetAddress.getByName("0.0.0.0");
        	System.out.println("Main launched with port "+port);
            ServerSocket srv = new ServerSocket(port,1, host);
            	Socket socket = srv.accept();
            	OwnHttpServer myownserver = new OwnHttpServer(socket, nomSite);
                System.out.println("Server started on port "+port);
        		Thread thread = new Thread(myownserver);
        		thread.start();    
            }
            
            
            
            
            
        }
		
		

	}

}
