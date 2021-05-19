package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

public class RunServer extends Thread{

	private int port;
	
	public RunServer(int port) throws IOException, URISyntaxException {
		this.port=port;
	}
	
	public void run() {
		try {
			// Serveur local
			InetAddress host = InetAddress.getByName("0.0.0.0");
			// Ecoute locale sur le port donné
	        ServerSocket srv = new ServerSocket(this.port,1, host);
            System.out.println("Server started on port "+port);
	        while(true) {
	        	// Attente d'une requête
	        	Socket socket = srv.accept();
	        	OwnHttpServer myownserver = new OwnHttpServer(socket);
	    		Thread thread = new Thread(myownserver);
	    		thread.start();    
	        }
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
