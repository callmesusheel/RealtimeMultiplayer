package com.bsb.games.multiplayer.actiondata;

import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class DisconnectRequest implements RequestData{
	public MultiplayerActionType type;
	public String gameId;
	public PlayerDetails user = new PlayerDetails();
}
