package fr.ul.miage.ownhttpserver;

public class Request {
	String base64Authentification;
	String host;
	boolean isBasicAuth;
	RequestType requestType;
	String requestURI;


	public Request(String requestString) {
		this.setRequestFromString(requestString);
	}

	public void setRequestFromString(String request) {
		String[] lines = request.split("\n");

		for (String line : lines) {
			setHeaderFromString(line);
		}
	}

	public void setHeaderFromString(String line) {
		String[] words = line.split(" ");
		System.out.println(line);
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
			this.host = this.host.replaceAll(" ", "");
			System.out.println("this.host : " + this.host);
			switch (this.host) {
			case "miniweb.miage":
				this.host = "miniweb";
				break;
			case "dopetrope.miage":
				this.host = "dopetrope";
				break;
			case "projethtmldut.miage":
				this.host = "projethtmldut";
				break;
			case "verti.miage":
				this.host = "verti";
				break;
			default:
				this.host=null;
				break;
			}
			System.out.println("nom d'hôte : " + words[1]);
			System.out.println("Répertoire sélectionné : " + this.host);
			break;
		}
	}

	public void setAuthorizationHeader(String type, String value) {
		this.base64Authentification = value;
	}

}