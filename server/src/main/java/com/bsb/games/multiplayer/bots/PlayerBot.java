package com.bsb.games.multiplayer.bots;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.response.SendMessageResponse;
import com.google.gson.Gson;

public abstract class PlayerBot {

	protected List<PlayerDetails> players;
	protected String roomId;
	protected PlayerDetails botPlayer;
	
	public PlayerBot() {
		this.botPlayer = generateBotPlayer();
	}
	
	public PlayerBot(PlayerDetails player) {
		this.botPlayer = player;
	}

	public void onMatchMakingDone(String roomId, List<PlayerDetails> players) {
		this.roomId = roomId;
		this.players = players;
	}

	public void onMessage(PlayerDetails player, byte[] data) {

	}

	public void onPlayerLeaveRoom(String roomId, PlayerDetails player) {

	}
	
	public PlayerDetails getBotPlayer() {
		return botPlayer;
	}

	public void setBotPlayer(PlayerDetails botPlayer) {
		this.botPlayer = botPlayer;
	}
	
	private PlayerDetails generateBotPlayer() {
		PlayerDetails player = new PlayerDetails();
		player.id = UUID.randomUUID().toString();
		player.name = NameGenerator.getRandomName();
		return player;
	}
	
	protected void sendMessage(byte[] data) {
		SendMessageResponse sendMessageResponse = new SendMessageResponse();
		sendMessageResponse.payload.sender = botPlayer;
		sendMessageResponse.payload.data = data;
		sendMessageResponse.payload.messageType = "SEND_MESSAGE";
		sendMessageResponse.payload.roomId = roomId;
		Room gameRoom = RealtimeData.getRealtimeData().getRoom(roomId);
		for (Player gamePlayer : gameRoom.getPlayers()) {
			Logger.getLogger(getClass().getCanonicalName()).info("gamePlayer : "+gamePlayer.getName());
			gamePlayer.getSession().getAsyncRemote().sendText(new Gson().toJson(sendMessageResponse));
		}
	}

}
