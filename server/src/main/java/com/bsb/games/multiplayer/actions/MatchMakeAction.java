package com.bsb.games.multiplayer.actions;

import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.MatchRoomResponse;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.google.gson.Gson;

public class MatchMakeAction {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void onMessage(final String message, final Session session) {
		final MatchRoomResponse response = new Gson().fromJson(message, MatchRoomResponse.class);
		final Player player = new Player(response.playerDetails.id, response.playerDetails.name, session);
		if (response.playerDetails.moreDetails != null) {
			player.setMoreDetails(response.playerDetails.moreDetails);
		}
		logger.info("matchmake");
		try {
			Room room = RealtimeData.getRealtimeData().addPlayerToAvailableRoom(response.gameId, response.gameId + "|" + response.matchMakeKey,
					player);
			if (room != null) { // match making done
				for (Player currentPlayer : room.getPlayers()) {
					sendResponseOfMatchMakeDone(room, currentPlayer);
				}
			} else {
				/**
				 * wait for 30-45 seconds,check if player is already part of any
				 * room, if not, then remove the player from available room and
				 * match him with the bot.
				 */
				int botMatchDelay = new Random().nextInt((45 - 30) + 1) + 30;
				new java.util.Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						Room availableRoom = RealtimeData.getRealtimeData().getAvailableRoom(response.gameId + "|" + response.matchMakeKey, player);
						if (availableRoom == null) {
							return;
						}
						sendErrorResponse(message, session);
						try {
							session.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						// try {
						// Room availableRoom =
						// RealtimeData.getRealtimeData().getAvailableRoom(response.gameId
						// + "|" + response.matchMakeKey,
						// player);
						// if (availableRoom == null) {
						// return;
						// }
						// RealtimeData.getRealtimeData().removeAvailableRoom(availableRoom);
						//
						// int playerSize = availableRoom.getPlayers().size();
						// int minPlayersToStart =
						// RealtimeData.getRealtimeData().getGameConfig(response.gameId).getMinPlayersToStart();
						// int requiredBots = minPlayersToStart - playerSize;
						// if (requiredBots < 1) {
						// return;
						// }
						// while (requiredBots > 0) {
						// Class<?> clazz =
						// RealtimeData.getRealtimeData().getGameConfig(response.gameId).getBotClass();
						// Constructor<?> ctor = clazz.getConstructor();
						// PlayerBot bot = (PlayerBot) ctor.newInstance(new
						// Object[] {});
						// logger.info("creating bot : "+bot.getBotPlayer().name);
						// availableRoom.addBot(bot);
						// requiredBots--;
						// bot.onMatchMakingDone(availableRoom.getRoomId(),
						// availableRoom.getPlayersDetails());
						// }
						// RealtimeData.getRealtimeData().addRoom(availableRoom);
						// sendResponseOfMatchMakeDone(availableRoom, player);
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
					}
				}, botMatchDelay * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendErrorResponse(message, session);
		}
	}

	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		response.action = MultiplayerActionType.ERROR;
		response.error = MultiplayerActionType.MATCHMAKE;
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	public void sendResponseOfMatchMakeDone(Room room, Player currentPlayer) {
		MatchRoomResponse response = new MatchRoomResponse();
		response.action = MultiplayerActionType.MATCHMAKE;
		response.roomId = room.getRoomId();
		response.playerDetails.id = currentPlayer.getId();
		response.playerDetails.name = currentPlayer.getName();
		response.playerDetails.moreDetails = currentPlayer.getMoreDetails();

		for (Player player : room.getPlayers()) {
			PlayerDetails playerDetails = new PlayerDetails();
			playerDetails.id = player.getId();
			playerDetails.name = player.getName();
			response.roomPlayers.add(playerDetails);
		}

		for (PlayerBot bot : room.getBots()) {
			if (bot == null) {
				return;
			}
			PlayerDetails playerDetails = new PlayerDetails();
			playerDetails.id = bot.getBotPlayer().id;
			playerDetails.name = bot.getBotPlayer().name;
			playerDetails.moreDetails = bot.getBotPlayer().moreDetails;
			response.roomPlayers.add(playerDetails);
		}

		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserJoin : " + responseString);

		currentPlayer.getSession().getAsyncRemote().sendText(responseString);
	}

}
