package com.bsb.games.multiplayer.response;

import java.util.ArrayList;
import java.util.List;

public class ExitRoomResponse extends ActionResponse {
	public PayloadBean payload = new PayloadBean();

	public static class PayloadBean {
		public PlayerDetails leavingPlayer = new PlayerDetails();
		public List<PlayerDetails> participants = new ArrayList<PlayerDetails>();
		public String roomId;
		public MultiplayerActionType type = MultiplayerActionType.EXIT_ROOM;
	}
}