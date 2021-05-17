package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Main {
	

	static final int port = 4005;
	
	public static void main(String[] args) throws IOException {
		
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream inputStream = loader.getResourceAsStream("properties");
	    prop.load(inputStream);
	    System.out.println(prop.getProperty("miniweb.miage"));
		String resourceMiniweb = prop.getProperty("miniweb.miage");
		
		InetAddress host = InetAddress.getByName("0.0.0.0");
    	System.out.println("Main launched with port "+port);
        ServerSocket srv = new ServerSocket(port,1, host);
        while(true) {
        	Socket socket = srv.accept();
        	OwnHttpServer myownserver = new OwnHttpServer(socket, "miniweb");
            System.out.println("Server started on port "+port);
            
    		Thread thread = new Thread(myownserver);
    		thread.start();    
        }
	}

}
