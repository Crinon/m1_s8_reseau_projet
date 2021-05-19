package fr.ul.miage.ownhttpserver;

public class Request {
	public String base64Authentification;
	public String host;
	public boolean isBasicAuth;
	public RequestType requestType;
	public String requestURI;
	public String resource;


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
			setHostName(words[1].split(":")[0].replaceAll(" ", ""));
			break;
		}
	}
	
	public void setHostName(String hostName) {
		for(String host : Main.sites.keySet()) {
			if(host != null && host.equalsIgnoreCase(hostName)) {
				this.resource = Main.sites.get(host);
				break;
			}
		}
	}

	public void setAuthorizationHeader(String type, String value) {
		this.base64Authentification = value;
	}

}