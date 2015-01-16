package com.bsb.games.multiplayer.response;



public class SendMessageResponse {
	public String roomId;
	public String gameId;
	public MultiplayerActionType action;
	public byte[] data;
	public PlayerDetails playerDetails = new PlayerDetails();
	
}
