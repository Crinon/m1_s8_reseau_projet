package fr.ul.miage.ownhttpserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class OwnHttpServer implements Runnable{ 
	
	
	// Constantes
	private static final String HOMEPAGE = "/";
	private static final String PASSWORDFILE = ".htpasswd";
	private static final String KEEPALIVE = "Connection: keep-alive";
	
	// Messages
	
	public static final String adressMessage = "adresse IP : %s";
	public static final String runMessage = "Traitement d'une socket";
	
	//configurations
	public static String notFoundPageName = "404.html";

	// Attribut
	private Socket socket;
	
	private String homepage = "index.html";
	private String resourcesName= "miniweb";
	private File resourceFolder;

	public OwnHttpServer(Socket socket, String resourcesName) {
		this.resourcesName = resourcesName;
		this.socket=socket;
		this.resourceFolder = new File(getClass().getResource("/").getPath()+"/"+this.resourcesName);
		System.out.println(this.resourceFolder);
	}

	@Override
	public void run() {
		System.out.println(OwnHttpServer.runMessage);
		System.out.println(String.format(OwnHttpServer.adressMessage, this.socket.getInetAddress()));
        // D_claration du tableau de String pour stocker les diff_rents _l_ments de la requ_te du navigateur
        String[] partie;
        // D_claration d'un tableau de byte pour convertir les fichers _ envoyer au navigateur
       
        // D_claration du String qui recevra les noms de fichiers demand_s par l'utilisateur (retrouv_ dans "requete")
        String nomFichier;	
        BufferedReader rd;
		try {
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf8"));
	        // Initialisation du protocole de reception de flux
			DataOutputStream data = new DataOutputStream(socket.getOutputStream());
			
			String requestString = "";
			String inputLine;
			
            while ((inputLine = rd.readLine()) != null) {
            	if(! "".equals(inputLine)) {
            		requestString+=inputLine+"\n";
            	}else {
            		break;
            	}
            }
            
            
            if("".equals(requestString)) {
            	return;
            }
            
            
            Request request = new Request(requestString);
            
	        
	        // Si on ne met pas d'URI on donne la homepage
            if (HOMEPAGE.equals(request.requestURI)) {
                nomFichier = homepage;
            } else {
                nomFichier = request.requestURI;
            }
            
            System.out.println(nomFichier);
            
            File file = new File(this.resourceFolder.getAbsolutePath()+"/"+nomFichier);
            
            System.out.println("asked resource : "+file.getPath());
            
            if( !testPassword(file.getParentFile(), request.base64Authentification)) {
            	sendUnallowed(data);
            }else {
            	sendFileContent(nomFichier, data);
            }
            
            // Les donn_es, en bytes, de l'index sont envoy_es
            
            System.out.println("close");
            data.flush();
            data.close();
            rd.close();
            
	        
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			return;
		}


	}
	
	private void sendUnallowed(DataOutputStream data) throws IOException {
		data.writeBytes("HTTP/1.1 403 Forbidden\r\n");
	}

	private void sendFileContent(String nomFichier, DataOutputStream data) throws IOException {
		
		//Path chemin = Paths.get(this.path+nomFichier);

		File file = new File(this.resourceFolder.getAbsolutePath()+"/"+nomFichier);
		
	
        // Le tableau de bytes va recevoir byte par byte ceux composant le fichier
        // Envoi du message de bonne reception de la requete de l'utilisateur
        if(!file.exists()) {
        	data.writeBytes("HTTP/1.1 404 Not Found\r\n");
        	file = new File(this.resourceFolder.getAbsolutePath()+"/"+notFoundPageName);
        }else if(file.isDirectory()){
        	data.writeBytes("HTTP/1.1 404 Not Found\r\n");
        	file = new File(this.resourceFolder.getAbsolutePath()+"/"+notFoundPageName);
        }else {
        	data.writeBytes("HTTP/1.1 200 OK\r\n");
        	
        	if (nomFichier.endsWith(".html")) {
                // Mention du type de donn_es envoy_es, ici du texte
                data.writeBytes("Content-Type: text/html\r\n");				
    		}
    		if (nomFichier.endsWith(".gif") || nomFichier.endsWith(".jpg")) {
                // Ici, le contenu envoy_ n'est plus du texte mais une image
                data.writeBytes("content-type:image/gif\r\n");
    		}
        }
		
        writeFileContent(data,file);
	}

	
	
	
	private void writeFileContent(DataOutputStream data, File file) throws IOException {
		byte[] tableau = null;
		if(file.exists()) {
		Path path = Paths.get(file.getPath());
        tableau = Files.readAllBytes(path);
        
		DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
		
        // Mention de la longueur du fichier qui va _tre transmis (m_thode available())
        data.writeBytes("Content-Length: " + Integer.toString(fileIn.available()) + "\r\n");
        // Ligne vide avant l'envoi du tableau de bytes
        fileIn.close();
		}
        data.writeBytes("\r\n");
        if(tableau != null) {
            data.write(tableau);
        }
        
	}

	//authentification (pas de md5 pour le moment)
	public boolean testPassword(File folder, String base64) throws IOException {
		File passwordFile = new File(folder.getPath()+File.separator+OwnHttpServer.PASSWORDFILE);
		System.out.println("password: "+passwordFile.getPath());
		if(!passwordFile.exists()) {
			return true;
		}
		
			if(base64 == null || "".equalsIgnoreCase(base64))
				return false;
			
			String[] credentials = new String(java.util.Base64.getDecoder().decode(base64)).split(":");
			
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
				String line; 
				while((line = reader.readLine()) != null) {
					String[] words = line.split(":");
					String userName = words[0];
					String password = words[1];
					System.out.println(userName);
					System.out.println(password);
					if(userName.equalsIgnoreCase(credentials[0]) && 
							password.equalsIgnoreCase(credentials[1]))
						return true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
		}
		return false;
	}

}