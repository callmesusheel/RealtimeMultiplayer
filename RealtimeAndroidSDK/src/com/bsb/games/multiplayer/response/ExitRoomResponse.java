package com.bsb.games.multiplayer.response;

import java.util.ArrayList;
import java.util.List;

public class ExitRoomResponse extends ActionResponse {
	public PayloadBean payload;

	public static class PayloadBean {
		public PlayerDetails leavingPlayer;
		public List<PlayerDetails> participants = new ArrayList<PlayerDetails>();
		public String roomId;
		public MultiplayerActionType type = MultiplayerActionType.EXIT_ROOM;
	}
}