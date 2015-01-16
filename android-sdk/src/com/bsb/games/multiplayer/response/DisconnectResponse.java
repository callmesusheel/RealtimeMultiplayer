package com.bsb.games.multiplayer.response;

public class DisconnectResponse {
	public String roomId;
	public String matchMakeKey;
	public MultiplayerActionType action;
	public String gameId;
	public PlayerDetails playerDetails = new PlayerDetails();
}
