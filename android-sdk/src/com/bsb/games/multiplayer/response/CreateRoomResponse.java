package com.bsb.games.multiplayer.response;

public class CreateRoomResponse {

	public String roomId;
	public String gameId;
	public String matchKey;
	public MultiplayerActionType action;
	public PlayerDetails playerDetails = new PlayerDetails();

}
