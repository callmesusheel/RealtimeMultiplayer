package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;

public class PingRequest {
	public String type = "GAME";
	public Payload data;
	
	class Payload {
		public MultiplayerActionType type;
	}
}
