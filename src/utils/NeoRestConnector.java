package utils;

import rest.RestAPI;
import rest.RestAPIFacade;

public class NeoRestConnector {
	private RestAPI restAPI;
	private static final String SERVER_ROOT_URI = "http://164.125.50.124:7474/db/data/";
	public NeoRestConnector(){
		restAPI = new RestAPIFacade(SERVER_ROOT_URI);
	}
			
	public RestAPI getRestAPI(){
		return this.restAPI;
	}
	
	public void clearDB(){
		String cypher = "START n=node(*) MATCH n-[r?]-() WHERE ID(n) <> 0 DELETE n,r";
		this.restAPI.query(cypher, null);
	}
}
