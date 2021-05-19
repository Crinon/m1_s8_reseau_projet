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
import java.util.Base64;

public class OwnHttpServer implements Runnable {
	// Requête à rediriger
	private static final String HOMEPAGEREDIRECTION = "/";
	// Nom du fichier pour protéger un site
	private static final String PASSWORDFILE = ".htpasswd";

	// Messages
	public static final String adressMessage = "Remote IP : %s";
	public static final String runMessage = "Process socket";

	// Fichier a envoyer en cas de fichier non trouvé
	public static String notFoundPageName = "404.html";

	// Attribut
	private Socket socket;
	private String resourcePath;
	private String folderpage = "/help.html";
	private String homepage = "index.html";
	private File resourceFolder;

	public OwnHttpServer(Socket socket) throws URISyntaxException {
		this.socket = socket;
		this.resourcePath = getClass().getResource("/").toURI().getPath();
	}

	@Override
	public void run() {
		// String recevant le nom du fichier requêté
		String nomFichier;
		BufferedReader rd;
		try {
			// Lecture du flux entrant et encodage en utf8
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
			// Initialisation du protocole de reception de flux
			DataOutputStream data = new DataOutputStream(socket.getOutputStream());
			// On stock la requête dans requestString
			String requestString = "";
			String inputLine;
			while ((inputLine = rd.readLine()) != null) {
				if (!"".equals(inputLine)) {
					requestString += inputLine + "\n";
				} else {
					break;
				}
			}

			// Si la requête est vide on arrête
			if ("".equals(requestString)) {
				return;
			}

			// Création d'un objet Request qui parsera les headers de la requête
			Request request = new Request(requestString);
			// Dossier du site demandé
			this.resourceFolder = new File(this.resourcePath + "/" + request.resource);

			// Si on ne met pas d'URI on donne la homepage index.html)
			if (HOMEPAGEREDIRECTION.equals(request.requestURI)) {
				nomFichier = homepage;
				// Si on demande le listing du repertoire, on ne charge pas de fichier
			} else if (folderpage.equals(request.requestURI)) {
				nomFichier = "";
			} else {
				nomFichier = request.requestURI;
			}

			// Ouverture du fichier concerné par la requête
			File file = new File(this.resourceFolder.getAbsolutePath() + "/" + nomFichier);
			// Cas de dossier racine de resources
			if ("".equals(nomFichier)) {
				file = new File(this.resourceFolder.getAbsolutePath());
			}

			// On regarde si le site est protégé par Basic HTTP Auth et on regarde si le mot
			// de passe est correct
			if (!testPassword(file.getParentFile(), request.base64Authentification)) {
				sendLogin(data);
			} else {
				// Si il n'y a pas de protection ou que la personne a envoyé les bonnes
				// informations de connexion, on envoie le fichier
				sendFileContent(nomFichier, data, folderpage.equals(request.requestURI));
			}

			// On vide les buffer
			data.flush();
			// Fermeture du buffer
			data.close();
			// Fermeture du buffer
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

	// Fonction qui ouvre la fenêtre de basic HTP auth
	private void sendLogin(DataOutputStream data) throws IOException {
		data.writeBytes("HTTP/1.1 401 Unauthorized\r\n");
		data.writeBytes("WWW-Authenticate: Basic");
	}

	// Codage en byte du fichier à envoyer
	private void sendFileContent(String nomFichier, DataOutputStream data, boolean root) throws IOException {
		// Fichier concerné par la requête
		File file = new File(this.resourceFolder.getAbsolutePath() + "/" + nomFichier);
		// Si le fichier n'existe pas et que le fichier 404.html n'existe pas non plus
		if (!file.exists()) {
			data.writeBytes("HTTP/1.1 404 Not Found\r\n");
			file = new File(this.resourceFolder.getAbsolutePath() + "/" + notFoundPageName);
		} else if (file.isDirectory()) {
			// Cas spécifique du listing de répertoire
			data.writeBytes("HTTP/1.1 200 OK\r\n");
			data.writeBytes("Content-Type: text/html\r\n");
		} else {
			data.writeBytes("HTTP/1.1 200 OK\r\n");
			if (nomFichier.endsWith(".html")) {
				data.writeBytes("Content-Type: text/html\r\n");
			}
			if (nomFichier.endsWith(".gif") || nomFichier.endsWith(".jpg")) {
				// Ici, le contenu envoyé est une image
				data.writeBytes("content-type:image/gif\r\n");
			}
		}
		// Envoi du ficher
		writeFileContent(data, file, root);
	}

	// Fonction pour gérer les headers et le contenu du fichier
	private void writeFileContent(DataOutputStream data, File file, boolean root) throws IOException {
		byte[] tableau = null;
		if (file.exists()) {
			// Listing de répertoire
			if (file.isDirectory()) {
				writeFolderHierarchy(data, file, root);
				return;
			}
			// Chemin du fichier
			Path path = Paths.get(file.getPath());
			tableau = Files.readAllBytes(path);
			// Récupération du contenu du fichier
			DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
			// Mention de la longueur du fichier qui va être transmis
			data.writeBytes("Content-Length: " + Integer.toString(fileIn.available()) + "\r\n");
			fileIn.close();
		}
		// Ligne vide avant l'envoi du tableau de bytes
		data.writeBytes("\r\n");
		if (tableau != null) {
			data.write(tableau);
		}
	}

	// Fonctionnalité d'affichage de listing des fichiers
	private void writeFolderHierarchy(DataOutputStream data, File folder, boolean root) throws IOException {
		StringBuilder htmlCode = new StringBuilder();
		// On regarde le nom du dossier actuel s'il existe
		String start = folder.getName() + "/";
		if (root)
			start = "/";
		for (File file : folder.listFiles()) {
			// Généraiton du lien
			htmlCode.append("<a href=\"" + start + file.getName() + "\">" + file.getName() + "</a></br>");
		}
		// Envoi de la taille du fichier généré
		data.writeBytes("Content-Length: " + htmlCode.toString().getBytes().length + "\r\n");
		data.writeBytes("\r\n");
		// Envoi du fichier HTML généré
		data.write(htmlCode.toString().getBytes());
	}

	// Authentification HTTP basique, prend en argument le répertoire de la page et le login
	public boolean testPassword(File folder, String base64) throws IOException {
		// Ouverture du fichier de sécurité
		File passwordFile = new File(folder.getPath() + File.separator + OwnHttpServer.PASSWORDFILE);
		// En cas d'absence de fichier, la ressource est accessible
		if (!passwordFile.exists()) {
			return true;
		}
		// Si les informations de logins ne correspondent pas à ce qui est attendu
		if (base64 == null || "".equalsIgnoreCase(base64))
			return false;
		// On distingue le username de son mot de passe
		String[] credentials = new String(Base64.getDecoder().decode(base64)).split(":");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
			String line;
			// Lesture de tous les logins autorisés
			while ((line = reader.readLine()) != null) {
				String[] words = line.split(":");
				String userName = words[0];
				String password = words[1];
				// Si le login est correct alors l'accès est accepté
				if (userName.equalsIgnoreCase(credentials[0]) && password.equalsIgnoreCase(credentials[1])) {
					reader.close();
					return true;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Si l'authentification a échoué
		return false;
	}

}