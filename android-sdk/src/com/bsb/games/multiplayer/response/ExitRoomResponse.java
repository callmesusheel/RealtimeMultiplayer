package com.bsb.games.multiplayer.response;


public class ExitRoomResponse {
	public String roomId;
	public MultiplayerActionType action;
	public String gameId;
	public PlayerDetails playerDetails = new PlayerDetails();

}
