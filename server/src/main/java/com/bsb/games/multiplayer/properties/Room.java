package com.bsb.games.multiplayer.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.websocket.Session;

import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.response.PlayerDetails;

public class Room {

	private String roomId;
	private List<Player> players;
	private List<String> sessionIds;
	private String matchKey = "";
	private int maxAllowedPlayers = 6;
	private List<PlayerBot> bots;

	public List<String> getSessionIds() {
		return this.sessionIds;
	}

	public List<Player> getPlayers() {
		return players;
	}
	
	public List<PlayerDetails> getPlayersDetails() {
		List<PlayerDetails> playerDetailsList = new ArrayList<PlayerDetails>();
		for(Player player : players) {
			PlayerDetails playerDetails = new PlayerDetails();
			playerDetails.id = player.getId();
			playerDetails.name = player.getName();
			playerDetails.properties = player.getMoreDetails();
			playerDetailsList.add(playerDetails);
		}
		return playerDetailsList;
	}

	public void setMatchKey(String matchKey) {
		this.matchKey = matchKey;
	}

	public String getMatchKey() {
		return this.matchKey;
	}

	public boolean isBotPlaying() {
		if (bots != null && bots.size() > 0) {
			return true;
		}
		return false;
	}

	public void addBot(PlayerBot bot) {
		if (bots == null) {
			bots = new ArrayList<PlayerBot>();
		}
		bots.add(bot);
	}

	public List<PlayerBot> getBots() {
		if (bots == null) {
			bots = new ArrayList<PlayerBot>();
		}
		return bots;
	}

	public void addPlayer(Player player) throws Exception {
		if (players.size() >= maxAllowedPlayers) {
			throw new Exception("Maximum capacity of room is reached");
		}
		this.players.add(player);
		this.sessionIds.add(player.getSession().getId());
	}

	public void removePlayer(Session session) {
		Player removePlayer = null;
		for (Player player : this.players) {
			if (player.getSession().getId().equals(session.getId())) {
				removePlayer = player;
			}
		}
		if (removePlayer != null) {
			removePlayer(removePlayer);
		}
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
		this.sessionIds.remove(player.getSession().getId());
		if (this.players.size() < 1) {
			RealtimeData.getRealtimeData().removeRoom(this);
		}
	}
	
	public void removeAvailablePlayer(Session session) {
		Player removePlayer = null;
		for (Player player : this.players) {
			if (player.getSession().getId().equals(session.getId())) {
				removePlayer = player;
			}
		}
		if (removePlayer != null) {
			removeAvailablePlayer(removePlayer);
		}
	}

	public void removeAvailablePlayer(Player player) {
		this.players.remove(player);
		this.sessionIds.remove(player.getSession().getId());
		if (this.players.size() < 1) {
			RealtimeData.getRealtimeData().removeRoom(this);
		}
	}

	public boolean hasSession(Session session) {
		for (Player player : this.players) {
			if (player.getSession().getId().equals(session.getId())) {
				return true;
			}
		}
		return false;
	}

	public Player getPlayer(Session session) {
		Player requiredPlayer = null;
		for (Player player : this.players) {
			if (player.getSession().getId().equals(session.getId())) {
				requiredPlayer = player;
			}
		}
		return requiredPlayer;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public Room(String roomId, String matchKey, Player player, int maxAllowedPlayers) {
		super();
		this.roomId = roomId;
		this.maxAllowedPlayers = maxAllowedPlayers;
		this.matchKey = matchKey;
		players = new ArrayList<Player>();
		players.add(player);

		sessionIds = new ArrayList<String>();
		sessionIds.add(player.getSession().getId());
	}

	public static String getRandomRoomId() {
		Random r = new Random();
		int i1 = r.nextInt(10000 - 1) + 1;
		return String.valueOf(i1);
		// UUID uuid = UUID.randomUUID();
		// return uuid.toString();
	}

}
