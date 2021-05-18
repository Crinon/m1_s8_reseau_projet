package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;

public class Main {
	// Accession via http://miniweb.miage:4003/ en changeant les ports
	public static void main(String[] args) throws IOException, URISyntaxException {
		Properties prop = new Properties();
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("properties");
		prop.load(inputStream);

		int port = Integer.parseInt(prop.getProperty("port"));
		String[] sites = prop.getProperty("sites").split(",");
		
        RunServer runserver = new RunServer(port, sites);
        runserver.start();
	}
}
