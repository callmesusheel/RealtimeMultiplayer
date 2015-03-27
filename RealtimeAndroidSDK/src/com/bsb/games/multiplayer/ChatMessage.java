package com.bsb.games.multiplayer;

import com.bsb.games.multiplayer.response.PlayerDetails;

public class ChatMessage {
	PlayerDetails player;
	String message;

	ChatMessage(PlayerDetails player, String message) {
		this.player = player;
		this.message = message;
	}
}
