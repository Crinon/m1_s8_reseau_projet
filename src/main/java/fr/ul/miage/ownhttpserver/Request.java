package fr.ul.miage.ownhttpserver;

public class Request {
	public String base64Authentification;
	public String host;
	public boolean isBasicAuth;
	public RequestType requestType;
	public String requestURI;
	public String resource;

	// Une requête contient le header de la requête du navigateur internet
	public Request(String requestString) {
		this.setRequestFromString(requestString);
	}

	// Découpage ligne par ligne de la requête
	public void setRequestFromString(String request) {
		String[] lines = request.split("\n");
		for (String line : lines) {
			setHeaderFromString(line);
		}
	}

	// Parsing de la requête
	public void setHeaderFromString(String line) {
		String[] words = line.split(" ");
//		System.out.println(line);
		switch (words[0]) {
		case "GET":
			this.requestType = RequestType.GET;
			this.requestURI = words[1];
		case "PUT":
			this.requestType = RequestType.PUT;
			this.requestURI = words[1];
		case "POST":
			this.requestType = RequestType.POST;
			this.requestURI = words[1];
			break;
		case "Authorization:":
			setAuthorizationHeader(words[1], words[2]);
			break;
		case "Host:":
			// On retire le port à droite
			this.host = words[1].split(":")[0];
			// On nettoie les espaces
			this.host = this.host.replaceAll(" ", "");
			// Récupération du DNS utilisé
			setHostName(words[1].split(":")[0].replaceAll(" ", ""));
			break;
		}
	}

	// On vérifie que le DNS correspond à l'un des sites enregistrés
	public void setHostName(String hostName) {
		for (String host : Main.sites.keySet()) {
			if (host != null && host.equalsIgnoreCase(hostName)) {
				this.resource = Main.sites.get(host);
				break;
			}
		}
	}

	public void setAuthorizationHeader(String type, String value) {
		this.base64Authentification = value;
	}

}