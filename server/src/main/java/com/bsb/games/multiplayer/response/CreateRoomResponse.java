package com.bsb.games.multiplayer.response;

import com.bsb.games.multiplayer.actions.MultiplayerActionType;


public class CreateRoomResponse {
	
	public String roomId;
	public String gameId;
	public String matchKey;
	public MultiplayerActionType action;
 	public PlayerDetails playerDetails = new PlayerDetails();

}
