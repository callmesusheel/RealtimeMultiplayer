package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.actiondata.JoinRoomRequest;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.JoinRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.google.gson.Gson;

public class JoinRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			JoinRoomRequest response = new Gson().fromJson(message, JoinRoomRequest.class);
			Player player = new Player(response.data.user.id, response.data.user.name, session);
			if (response.data.user.properties != null) {
				player.setMoreDetails(response.data.user.properties);
			}
			logger.info("joinroom");
			Room joinRoom = RealtimeData.getRealtimeData().getRoom(response.data.roomId);
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
		JoinRoomResponse response = new JoinRoomResponse();
		response.action = MultiplayerActionType.JOIN_ROOM;
		response.status.success = false;
		response.status.message = "Unable to join room";
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfError : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	private void sendResponseOfUserJoin(Player joiningPlayer, Room joinRoom) throws IOException {
		JoinRoomResponse response = new JoinRoomResponse();
		response.action = MultiplayerActionType.JOIN_ROOM;
		response.messageType = "GAME";
		response.status.success = true;
		response.status.message = "Joined room successfully";
		response.payload.roomId = joinRoom.getRoomId();
		response.payload.joiningPlayer.id = joiningPlayer.getId();
		response.payload.joiningPlayer.name = joiningPlayer.getName();
		response.payload.joiningPlayer.properties = joiningPlayer.getMoreDetails();

		for (Player player : joinRoom.getPlayers()) {
			PlayerDetails playerDetails = new PlayerDetails();
			playerDetails.id = player.getId();
			playerDetails.name = player.getName();
			response.payload.participants.add(playerDetails);
		}

		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserJoin : " + responseString);

		for (Player player : joinRoom.getPlayers()) {
			player.getSession().getAsyncRemote().sendText(responseString);
		}
	}

}
