package com.bsb.games.multiplayer.bots;

import java.util.List;

import com.bsb.games.multiplayer.response.PlayerDetails;

public class SDKTestAppBot extends PlayerBot {

	public SDKTestAppBot() {
		super();
	}

	@Override
	public void onMatchMakingDone(String roomId, List<PlayerDetails> players) {
		super.onMatchMakingDone(roomId, players);
		/**
		 * At this point match has started. Write code to perform appropriate
		 * game action
		 */
	}

	@Override
	public void onMessage(PlayerDetails player, byte[] data) {
		super.onMessage(player, data);
		sendMessage(data);
	}

	@Override
	public void onPlayerLeaveRoom(String roomId, PlayerDetails player) {
		super.onPlayerLeaveRoom(roomId, player);
		/**
		 * Remove bot from room
		 */
	}

}
