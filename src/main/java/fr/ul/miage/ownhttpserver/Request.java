package fr.ul.miage.ownhttpserver;

public class Request {
	
	String base64Authentification;
	
	boolean isBasicAuth;
	
	RequestType requestType;
	String requestURI;
	
	public Request(String request) {
		this.setRequestFromString(request);
	}
	
	public void setRequestFromString(String request) {
		String[] lines = request.split("\n");
		
		for(String line : lines) {
			setHeaderFromString(line);
		}
	}
	
	public void setHeaderFromString(String line) {
		String[] words = line.split(" ");
		System.out.println(words[0]);
		switch(words[0]) {
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
		}
	}
	
	public void setAuthorizationHeader(String type, String value) {
		this.base64Authentification = value;
	}

}