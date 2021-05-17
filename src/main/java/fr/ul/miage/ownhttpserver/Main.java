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
        Enumeration<String> enums = (Enumeration<String>) prop.propertyNames();
        while (enums.hasMoreElements()) {
          String key = enums.nextElement();
          int value = Integer.parseInt(prop.getProperty(key));
          System.out.println(key + " : " + value);
          RunServer runserver = new RunServer(key,value);
          System.out.println("Démarrage de " + key);
          runserver.start();
          System.out.println("Suivant");
        }
	}
}
