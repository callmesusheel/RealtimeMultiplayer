package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.actiondata.ExitRoomRequest;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ExitRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.google.gson.Gson;

public class ExitRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			ExitRoomRequest response = new Gson().fromJson(message, ExitRoomRequest.class);
			Player player = new Player(response.data.user.id, response.data.user.name, session);
			if(response.data.user.properties!=null) {
				player.setMoreDetails(response.data.user.properties);
			}
			Room removeRoom = RealtimeData.getRealtimeData().getRoom(response.data.roomId);
			if (removeRoom != null) {
				removeRoom.removePlayer(player);
				sendResponseOfUserExit(player, removeRoom);
			} else {
				RealtimeData.getRealtimeData().removeSessionFromAvailableRoom(session);
			}
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponseOfUserExit(Player leavingPlayer, Room removeRoom) throws IOException {
		ExitRoomResponse response = new ExitRoomResponse();
		response.action = MultiplayerActionType.EXIT_ROOM;
		response.messageType = "GAME";
		response.status.success = true;
		response.status.message = "Player left room";
		response.payload.roomId = removeRoom.getRoomId();
		response.payload.leavingPlayer.id = leavingPlayer.getId();
		response.payload.leavingPlayer.name = leavingPlayer.getName();
		response.payload.leavingPlayer.properties = leavingPlayer.getMoreDetails();
		
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);

		for (Player player : removeRoom.getPlayers()) {
			if (!player.getId().equals(leavingPlayer.getId())) {
				player.getSession().getAsyncRemote().sendText(responseString);
			}
		}
	}

}
