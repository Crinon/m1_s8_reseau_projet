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
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OwnHttpServer implements Runnable{ 
	// Constantes
	private static final String HOMEPAGE = "/";
	private static final String PASSWORDFILE = ".htpasswd";
	
	// Messages
	public static final String adressMessage = "adresse IP : %s";
	public static final String runMessage = "Traitement d'une socket";
	
	// Configurations
	public static String notFoundPageName = "404.html";

	// Attribut
	private Socket socket;
	private String resourcePath;
	private String folderpage = "/help.html";
	private String homepage = "index.html";
	private File resourceFolder;
	private String[] sitesList ;

	public OwnHttpServer(Socket socket, String[] sitesList) throws URISyntaxException {
		this.socket=socket;
		this.sitesList = sitesList;
		this.resourcePath = getClass().getResource("/").toURI().getPath();
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
    		this.resourceFolder = new File(this.resourcePath+"/"+request.host);
    		System.out.println("------"+this.resourceFolder.getPath()+"-------");

	        // Si on ne met pas d'URI on donne la homepage
            if (HOMEPAGE.equals(request.requestURI)) {
                nomFichier = homepage;
            } else if (folderpage.equals(request.requestURI)){
            	nomFichier = "";
            }else{
                nomFichier = request.requestURI;
            }

            
            File file  = new File(this.resourceFolder.getAbsolutePath()+"/"+nomFichier);
            if(nomFichier.equals("")) {
            	file =  new File(this.resourceFolder.getAbsolutePath());
            }
            
            if( !testPassword(file.getParentFile(), request.base64Authentification)) {
            	sendUnallowed(data);
            }else {
            	sendFileContent(nomFichier, data, folderpage.equals(request.requestURI));
            }
            
            // Les donn_es, en bytes, de l'index sont envoy_es
            
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

	private void sendFileContent(String nomFichier, DataOutputStream data, boolean root) throws IOException {
		File file = new File(this.resourceFolder.getAbsolutePath()+"/"+nomFichier);
		
		System.out.println("Le vrai path:"+file.getPath());
	
        // Le tableau de bytes va recevoir byte par byte ceux composant le fichier
        // Envoi du message de bonne reception de la requete de l'utilisateur
        if(!file.exists()) {
        	System.out.println("pas exist");
        	data.writeBytes("HTTP/1.1 404 Not Found\r\n");
        	file = new File(this.resourceFolder.getAbsolutePath()+"/"+notFoundPageName);
        }else if(file.isDirectory()){
        	System.out.println("folder help");
        	data.writeBytes("HTTP/1.1 200 OK\r\n");
        	data.writeBytes("Content-Type: text/html\r\n");
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
		
        writeFileContent(data,file, root);
	}

	
	
	
	private void writeFileContent(DataOutputStream data, File file, boolean root) throws IOException {
		byte[] tableau = null;
		if(file.exists()) {
				if(file.isDirectory()) {
					writeFolderHierarchy(data,file, root);
					return;
				}
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
	
	private void writeFolderHierarchy(DataOutputStream data, File folder, boolean root) throws IOException {
		StringBuilder htmlCode = new StringBuilder();
		String start = folder.getName()+"/";
		if(root)
			start = "/";
		for(File file : folder.listFiles()) {
			htmlCode.append("<a href=\""+start+file.getName()+"\">"+file.getName()+"</a></br>");
		}
		
		data.writeBytes("Content-Length: " + htmlCode.toString().getBytes().length + "\r\n");
		data.writeBytes("\r\n");
		data.write(htmlCode.toString().getBytes());
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