package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.ExitRoomResponse;
import com.bsb.games.multiplayer.response.RoomResponse;
import com.google.gson.Gson;

public class ExitRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			ExitRoomResponse response = new Gson().fromJson(message, ExitRoomResponse.class);
			Player player = new Player(response.playerDetails.id, response.playerDetails.name, session);
			if(response.playerDetails.moreDetails!=null) {
				player.setMoreDetails(response.playerDetails.moreDetails);
			}
			Room removeRoom = RealtimeData.getRealtimeData().getRoom(response.roomId);
			if (removeRoom != null) {
				removeRoom.removePlayer(player);
				sendResponseOfUserExit(player, removeRoom);
			} else {
				RealtimeData.getRealtimeData().removeSessionFromAvailableRoom(session);
			}
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
			sendErrorResponse(message, session);
		}
	}

	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		response.action = MultiplayerActionType.ERROR;
		response.error = MultiplayerActionType.EXITROOM;
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	public void sendResponseOfUserExit(Player leavingPlayer, Room removeRoom) throws IOException {
		RoomResponse response = new RoomResponse();
		response.action = MultiplayerActionType.EXITROOM;
		response.roomId = removeRoom.getRoomId();
		response.playerDetails.id = leavingPlayer.getId();
		response.playerDetails.name = leavingPlayer.getName();
		response.playerDetails.moreDetails = leavingPlayer.getMoreDetails();
		
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);

		for (Player player : removeRoom.getPlayers()) {
			if (!player.getId().equals(leavingPlayer.getId())) {
				player.getSession().getAsyncRemote().sendText(responseString);
			}
		}
		for(PlayerBot bot : removeRoom.getBots()) {
			bot.onPlayerLeaveRoom(removeRoom.getRoomId(), response.playerDetails);
		}
	}

}
