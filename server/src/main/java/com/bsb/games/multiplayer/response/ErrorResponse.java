package com.bsb.games.multiplayer.response;

import com.bsb.games.multiplayer.actions.MultiplayerActionType;


public class ErrorResponse {

	public String roomId;
	public MultiplayerActionType action;
	public MultiplayerActionType error;
	public String gameId;
	public PlayerDetails playerDetails = new PlayerDetails();

}
