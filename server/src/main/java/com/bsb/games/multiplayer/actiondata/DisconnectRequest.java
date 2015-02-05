package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class DisconnectRequest {
	public String type = "GAME";
	public Payload data;
	
	public class Payload {
		public MultiplayerActionType type;
		public String gameId;
		public PlayerDetails user = new PlayerDetails();
	}

}

