package fr.ul.miage.ownhttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class Main {
	// Hashmap pour avoir les noms des dossiers des sites et leur URL correspondant ("miniweb.miage":"miniweb")
	public static HashMap<String, String> sites;
	// Accession via http://miniweb.miage:4000/ (possibilité de modifier le port dans le fichier "properties")
	public static void main(String[] args) throws IOException, URISyntaxException {
		Main.sites = new HashMap<String, String>();
		Properties prop = new Properties();
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("properties");
		prop.load(inputStream);
		Enumeration<?> propertyNames = prop.propertyNames();
		System.out.println("Generating avalaible sites");
		while(propertyNames.hasMoreElements()) {
			String property = (String) propertyNames.nextElement();
			// On n'utilise pas le port pour générer la liste des sites
			if(!"Port".equalsIgnoreCase(property)) {
				Main.sites.put(property, prop.getProperty(property));
				System.out.println(property+":"+prop.getProperty(property));
			}
		}
		int port = Integer.parseInt(prop.getProperty("port"));
		System.out.println("Server's port : "+ port);
		// Démarrage du thread gérant le serveur
        RunServer runserver = new RunServer(port);
        runserver.start();
	}
}
