package com.bsb.games.multiplayer.properties;

import java.util.Map;

import javax.websocket.Session;

public class Player {

	private String id;
	private String name;
	private Session session;
	private Map<String,String>moreDetails;

	public Player(String id, String name, Session session) {
		super();
		this.id = id;
		this.name = name;
		this.session = session;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Session getSession() {
		return session;
	}
	
	public Map<String, String> getMoreDetails() {
		return moreDetails;
	}

	public void setMoreDetails(Map<String, String> moreDetails) {
		this.moreDetails = moreDetails;
	}

}
