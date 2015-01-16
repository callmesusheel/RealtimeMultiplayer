package com.bsb.games.multiplayer.actions;

import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.response.SendMessageResponse;
import com.google.gson.Gson;

public class SendMessageAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(String message, Session session) {
		try {
			SendMessageResponse response = new Gson().fromJson(message, SendMessageResponse.class);
			String responseString = new Gson().toJson(response);
			logger.info("onMessage : " + responseString);
			Player player = new Player(response.playerDetails.id, response.playerDetails.name, session);
			if(response.playerDetails.moreDetails!=null) {
				player.setMoreDetails(response.playerDetails.moreDetails);
			}
			Room gameRoom = RealtimeData.getRealtimeData().getRoom(response.roomId);
			for (Player gamePlayer : gameRoom.getPlayers()) {
				if (!gamePlayer.getId().equals(player.getId())) {
					gamePlayer.getSession().getAsyncRemote().sendText(responseString);
				}
			}
			for(PlayerBot bot : gameRoom.getBots()) {
				PlayerDetails playerDetails = new PlayerDetails();
				playerDetails.id = player.getId();
				playerDetails.name = player.getName();
				playerDetails.moreDetails = playerDetails.moreDetails;
				bot.onMessage(playerDetails, response.data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		response.action = MultiplayerActionType.ERROR;
		response.error = MultiplayerActionType.SENDMESSAGE;
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}
	
}
