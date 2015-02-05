package com.bsb.games.multiplayer.response;


public class ErrorResponse {

	public String roomId;
	public MultiplayerActionType action;
	public MultiplayerActionType error;
	public String gameId;
	public PlayerDetails playerDetails = new PlayerDetails();

}
