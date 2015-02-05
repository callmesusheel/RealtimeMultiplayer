package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.actiondata.CreateRoomRequest;
import com.bsb.games.multiplayer.properties.GameConfig;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.CreateRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.google.gson.Gson;

public class CreateRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			CreateRoomRequest response = new Gson().fromJson(message, CreateRoomRequest.class);
			Player player = new Player(response.data.user.id, response.data.user.name, session);
			if (response.data.user.properties != null) {
				player.setMoreDetails(response.data.user.properties);
			}
			logger.info("createroom with session Id : " + session.getId());
			String roomId = Room.getRandomRoomId();

			GameConfig gameConfig = RealtimeData.getRealtimeData().getGameConfig(response.data.gameId);
			logger.info("CreateRoomAction message : "+message);
			session.setMaxIdleTimeout(gameConfig.getMaxIdleTimeout());
			Room existRoom = RealtimeData.getRealtimeData().getRoom(session);
			if (existRoom != null) {
				sendErrorResponse(message, session);
				return;
			}
			Room newRoom = new Room(roomId, response.data.gameId + "|" + response.data.filter, player, gameConfig.getMaxAllowedPlayers());
			RealtimeData.getRealtimeData().addRoom(newRoom);
			sendResponseOfRoomCreation(player, newRoom);
		} catch (IOException e) {
			e.printStackTrace();
			sendErrorResponse(message, session);
		}
	}

	public void sendErrorResponse(String message, Session session) {
		CreateRoomResponse response = new CreateRoomResponse();
		response.action = MultiplayerActionType.CREATE_ROOM;
		response.status.success = false;
		response.status.message = "Unable to create room";
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfError : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	private void sendResponseOfRoomCreation(Player player, Room newRoom) throws IOException {
		CreateRoomResponse response = new CreateRoomResponse();
		response.messageType = "GAME";
		response.action = MultiplayerActionType.CREATE_ROOM;
		response.status.success = true;
		response.status.message = "Room Created Successfully";
		response.roomId = newRoom.getRoomId();
		response.user.id = player.getId();
		response.user.name = player.getName();
		response.user.properties = player.getMoreDetails();
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfRoomCreation : " + responseString);
		player.getSession().getAsyncRemote().sendText(responseString);
	}

}
