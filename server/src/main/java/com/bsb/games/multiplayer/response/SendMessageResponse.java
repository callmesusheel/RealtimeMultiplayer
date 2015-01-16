package com.bsb.games.multiplayer.response;

import com.bsb.games.multiplayer.actions.MultiplayerActionType;


public class SendMessageResponse {
	public String roomId;
	public String gameId;
	public MultiplayerActionType action;
	public byte[] data;
	public PlayerDetails playerDetails = new PlayerDetails();
	
}
