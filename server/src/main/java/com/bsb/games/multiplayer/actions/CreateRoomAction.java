package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.properties.GameConfig;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.CreateRoomResponse;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.RoomResponse;
import com.google.gson.Gson;

public class CreateRoomAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			CreateRoomResponse response = new Gson().fromJson(message, CreateRoomResponse.class);
			Player player = new Player(response.playerDetails.id, response.playerDetails.name, session);
			if(response.playerDetails.moreDetails!=null) {
				player.setMoreDetails(response.playerDetails.moreDetails);
			}
			logger.info("createroom with session Id : "+session.getId());
			String roomId = Room.getRandomRoomId();
			
			
			GameConfig gameConfig = RealtimeData.getRealtimeData().getGameConfig(response.gameId);
			session.setMaxIdleTimeout(gameConfig.getMaxIdleTimeout());
			Room existRoom = RealtimeData.getRealtimeData().getRoom(session);
			if(existRoom!=null) {
				sendErrorResponse(message, session);
				return;
			}
			Room newRoom = new Room(roomId,response.gameId + "|" + response.matchKey, player, gameConfig.getMaxAllowedPlayers());
			RealtimeData.getRealtimeData().addRoom(newRoom);
			sendResponseOfRoomCreation(player, newRoom);
		} catch (IOException e) {
			e.printStackTrace();
			sendErrorResponse(message, session);
		}
	}
	
	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		response.action = MultiplayerActionType.ERROR;
		response.error = MultiplayerActionType.CREATEROOM;
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}
	
	private void sendResponseOfRoomCreation(Player player, Room newRoom) throws IOException {
		RoomResponse response = new RoomResponse();
		response.action = MultiplayerActionType.CREATEROOM;
		response.roomId = newRoom.getRoomId();
		response.playerDetails.id = player.getId();
		response.playerDetails.name = player.getName();
		response.playerDetails.moreDetails = player.getMoreDetails();
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfRoomCreation : " + responseString);
		player.getSession().getAsyncRemote().sendText(responseString);
	}

}
