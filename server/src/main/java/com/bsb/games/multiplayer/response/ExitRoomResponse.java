package com.bsb.games.multiplayer.response;

import com.bsb.games.multiplayer.actions.MultiplayerActionType;


public class ExitRoomResponse {
	public String roomId;
	public MultiplayerActionType action;
	public String gameId;
	public PlayerDetails playerDetails = new PlayerDetails();

}
