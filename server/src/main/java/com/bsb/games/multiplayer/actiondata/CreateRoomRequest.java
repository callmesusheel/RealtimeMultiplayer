package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class CreateRoomRequest {
	public String type = "GAME";
	public Payload data;
	
	public class Payload {
		public String gameId;
		public String filter;
		public MultiplayerActionType type;
		public PlayerDetails user = new PlayerDetails();
	}
}

