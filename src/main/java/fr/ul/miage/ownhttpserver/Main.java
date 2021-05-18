package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class Main {
	
	public static HashMap<String, String> sites;
	// Accession via http://miniweb.miage:4003/ en changeant les ports
	public static void main(String[] args) throws IOException, URISyntaxException {
		Properties prop = new Properties();
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("properties");
		prop.load(inputStream);
		
		Main.sites = new HashMap<String, String>();
		Enumeration<?> propertyNames = prop.propertyNames();
		while(propertyNames.hasMoreElements()) {
			String property = (String) propertyNames.nextElement();
			if(!"Port".equalsIgnoreCase(property)) {
				Main.sites.put(property, prop.getProperty(property));
				System.out.println(property+":"+prop.getProperty(property));
			}
		}
		
		
		
		int port = Integer.parseInt(prop.getProperty("port"));
        RunServer runserver = new RunServer(port);
        runserver.start();
	}
}
