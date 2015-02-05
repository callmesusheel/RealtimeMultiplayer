package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.actiondata.DisconnectRequest;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ExitRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.google.gson.Gson;

public class DisconnectAction {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			DisconnectRequest response = new Gson().fromJson(message, DisconnectRequest.class);
			PlayerDetails leavingPlayer = response.data.user;
			Room removeRoom = RealtimeData.getRealtimeData().getRoom(session);
			if (removeRoom != null) {
				removeRoom.removePlayer(session);
				sendResponseOfUserExit(leavingPlayer, removeRoom);
			} else {
				RealtimeData.getRealtimeData().removeSessionFromAvailableRoom(session);
			}
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(Room room : RealtimeData.getRealtimeData().getRooms()) {
			logger.info("Rooms present : "+room.getRoomId());
		}
		
		for(Room room : RealtimeData.getRealtimeData().getAvailableRooms("test")) {
			logger.info("Available Rooms present : "+room.getRoomId());
		}
	}

	public void sendResponseOfUserExit(PlayerDetails leavingPlayer, Room removeRoom) throws IOException {
		ExitRoomResponse response = new ExitRoomResponse();
		response.action = MultiplayerActionType.EXIT_ROOM;
		response.messageType = "GAME";
		response.status.success = true;
		response.status.message = "Player left room";
		response.payload.roomId = removeRoom.getRoomId();
		response.payload.leavingPlayer.id = leavingPlayer.id;
		response.payload.leavingPlayer.name = leavingPlayer.name;
		response.payload.leavingPlayer.properties = leavingPlayer.properties;

		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);

		for (Player player : removeRoom.getPlayers()) {
			if (!player.getId().equals(leavingPlayer.id)) {
				player.getSession().getAsyncRemote().sendText(responseString);
			}
		}
	}
}
