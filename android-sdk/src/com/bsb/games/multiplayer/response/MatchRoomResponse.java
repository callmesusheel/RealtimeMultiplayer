package com.bsb.games.multiplayer.response;

import java.util.ArrayList;
import java.util.List;

public class MatchRoomResponse {

	public String roomId;
	public String gameId;
	public MultiplayerActionType action;
	public String matchMakeKey;
	public PlayerDetails playerDetails = new PlayerDetails();
	public List<PlayerDetails> roomPlayers = new ArrayList<PlayerDetails>();

}
