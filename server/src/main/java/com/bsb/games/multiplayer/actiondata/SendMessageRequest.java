package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class SendMessageRequest {
	public String type = "GAME";
	public Payload data;

	public class Payload {
		public String roomId;
		public String gameId;
		public MultiplayerActionType type;
		public String messageType;
		public byte[] data;
		public PlayerDetails user = new PlayerDetails();
	}
}
