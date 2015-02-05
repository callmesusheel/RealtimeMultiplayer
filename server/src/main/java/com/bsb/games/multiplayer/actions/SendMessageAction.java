package com.bsb.games.multiplayer.actions;

import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.actiondata.SendMessageRequest;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.SendMessageResponse;
import com.google.gson.Gson;

public class SendMessageAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			SendMessageRequest request = new Gson().fromJson(message, SendMessageRequest.class);
			String responseString = new Gson().toJson(request);
			logger.info("onMessage : " + responseString);
			Player player = new Player(request.data.user.id, request.data.user.name, session);
			if (request.data.user.properties != null) {
				player.setMoreDetails(request.data.user.properties);
			}
			Room gameRoom = RealtimeData.getRealtimeData().getRoom(request.data.roomId);
			sendMessageResponse(gameRoom, player,request.data.messageType,request.data.data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendErrorResponse(String message, Session session) {
		SendMessageResponse response = new SendMessageResponse();
		response.action = MultiplayerActionType.CREATE_ROOM;
		response.status.success = false;
		response.status.message = "Unable to send message";
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfError : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}
	
	public void sendMessageResponse(Room gameRoom, Player player, String messageType, byte[] data) {
		SendMessageResponse response = new SendMessageResponse();
		response.messageType = "GAME";
		response.action = MultiplayerActionType.SEND_MESSAGE;
		response.status.success = true;
		response.status.message = "Room Created Successfully";
		response.payload.sender.id = player.getId();
		response.payload.sender.name = player.getName();
		response.payload.messageType = messageType;
		response.payload.data = data;
		response.payload.roomId = gameRoom.getRoomId();
		String responseString = new Gson().toJson(response);
		for (Player gamePlayer : gameRoom.getPlayers()) {
			if (!gamePlayer.getId().equals(player.getId())) {
				gamePlayer.getSession().getAsyncRemote().sendText(responseString);
			}
		}
	}

}
