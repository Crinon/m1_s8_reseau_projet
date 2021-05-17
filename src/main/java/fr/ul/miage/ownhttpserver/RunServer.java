package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

public class RunServer extends Thread{

	private String resourceName;
	private int port;
	
	public RunServer(String resourceName, int port) throws IOException, URISyntaxException {
		this.resourceName=resourceName;
		this.port=port;
	}
	
	public void run() {
		try {
			InetAddress host;
			host = InetAddress.getByName("0.0.0.0");
	        ServerSocket srv = new ServerSocket(this.port,1, host);
	        while(true) {
	        	Socket socket = srv.accept();
	        	OwnHttpServer myownserver = new OwnHttpServer(socket, this.resourceName);
	            System.out.println("Server started on port "+port);
	    		Thread thread = new Thread(myownserver);
	    		thread.start();    
	        }
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}


	}
	
}
