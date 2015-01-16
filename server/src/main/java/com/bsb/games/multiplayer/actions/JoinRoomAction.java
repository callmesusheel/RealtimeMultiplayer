package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.JoinRoomResponse;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.google.gson.Gson;

public class JoinRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			JoinRoomResponse response = new Gson().fromJson(message, JoinRoomResponse.class);
			Player player = new Player(response.playerDetails.id, response.playerDetails.name, session);
			if (response.playerDetails.moreDetails != null) {
				player.setMoreDetails(response.playerDetails.moreDetails);
			}
			logger.info("joinroom");
			Room joinRoom = RealtimeData.getRealtimeData().getRoom(response.roomId);
			if (joinRoom == null) {
				sendErrorResponse(message, session);
				return;
			}
			joinRoom.addPlayer(player);
			sendResponseOfUserJoin(player, joinRoom);
		} catch (Exception e) {
			e.printStackTrace();
			sendErrorResponse(message, session);
		}
	}

	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		response.action = MultiplayerActionType.JOINROOM;
		response.error = MultiplayerActionType.ERROR;
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	private void sendResponseOfUserJoin(Player joiningPlayer, Room joinRoom) throws IOException {
		JoinRoomResponse response = new JoinRoomResponse();
		response.action = MultiplayerActionType.JOINROOM;
		response.roomId = joinRoom.getRoomId();
		response.playerDetails.id = joiningPlayer.getId();
		response.playerDetails.name = joiningPlayer.getName();
		response.playerDetails.moreDetails = joiningPlayer.getMoreDetails();

		for (Player player : joinRoom.getPlayers()) {
			PlayerDetails playerDetails = new PlayerDetails();
			playerDetails.id = player.getId();
			playerDetails.name = player.getName();
			response.roomPlayers.add(playerDetails);
		}

		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserJoin : " + responseString);

		for (Player player : joinRoom.getPlayers()) {
			player.getSession().getAsyncRemote().sendText(responseString);
		}
	}

}
