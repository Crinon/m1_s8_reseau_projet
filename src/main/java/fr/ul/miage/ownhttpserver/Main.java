package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

public class Main {
	

	static final int port = 4000;
	
	public static void main(String[] args) throws IOException, URISyntaxException {
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
