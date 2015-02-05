package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;


public class JoinRoomRequest implements RequestData{
	public String roomId;
	public String gameId;
	public MultiplayerActionType type;
	public PlayerDetails user = new PlayerDetails();
}
