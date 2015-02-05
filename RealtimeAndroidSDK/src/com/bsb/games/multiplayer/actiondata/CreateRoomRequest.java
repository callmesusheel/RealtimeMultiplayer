package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class CreateRoomRequest implements RequestData {

	public String gameId;
	public String filter;
	public MultiplayerActionType type;
	public PlayerDetails user = new PlayerDetails();

}
