package com.bsb.games.multiplayer.bots;

import java.util.List;

import com.bsb.games.multiplayer.response.PlayerDetails;

public interface BotImplementation {
	
	public void onMatchMakingDone(String roomId, List<PlayerDetails> players);
	
	public void onMessage(PlayerDetails player, byte[] data);

	public void onPlayerLeaveRoom(String roomId, PlayerDetails player);

}
