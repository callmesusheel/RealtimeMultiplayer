package com.bsb.games.multiplayer.response;

public class CreateRoomResponse extends ActionResponse {
	public String filter;
	public String gameId;
	public String roomId;
	public PlayerDetails user = new PlayerDetails();
}