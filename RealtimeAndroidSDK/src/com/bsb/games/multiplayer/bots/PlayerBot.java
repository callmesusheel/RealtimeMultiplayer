package com.bsb.games.multiplayer.bots;

import java.util.UUID;

import android.app.Activity;

import com.bsb.games.multiplayer.RealtimeMultiplayerClient.RealtimeMultiplayerEvents;
import com.bsb.games.multiplayer.response.PlayerDetails;

public abstract class PlayerBot implements BotImplementation{

	protected PlayerDetails botPlayer;
	private RealtimeMultiplayerEvents callback;
	protected Activity activity;
	
	public PlayerBot() {
		this.botPlayer = generateBotPlayer();
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public PlayerBot(PlayerDetails player) {
		this.botPlayer = player;
	}

	public PlayerDetails getBotPlayer() {
		return botPlayer;
	}

	public void setBotPlayer(PlayerDetails botPlayer) {
		this.botPlayer = botPlayer;
	}
	
	public void setCallback(RealtimeMultiplayerEvents callback) {
		this.callback = callback;
	}
	
	private PlayerDetails generateBotPlayer() {
		PlayerDetails player = new PlayerDetails();
		player.id = UUID.randomUUID().toString();
		player.name = NameGenerator.getRandomName();
		return player;
	}
	
	protected void sendMessage(final byte[] data) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				callback.onMessage(botPlayer, data);
			}
		});
		
	}

}
