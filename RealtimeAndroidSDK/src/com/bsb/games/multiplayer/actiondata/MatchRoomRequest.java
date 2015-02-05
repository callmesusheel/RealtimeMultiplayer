package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;


public class MatchRoomRequest implements RequestData{
	public String roomId;
	public String gameId;
	public MultiplayerActionType type;
	public String filter;
	public PlayerDetails user = new PlayerDetails();
}
