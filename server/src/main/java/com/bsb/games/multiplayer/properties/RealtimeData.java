package com.bsb.games.multiplayer.properties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.Session;

public class RealtimeData {

	private static RealtimeData instance;

	private List<Room> rooms;
	private Map<String, List<Room>> availableRooms;
	private Map<String, GameConfig> gameConfigs;

	public List<Room> getRooms() {
		return rooms;
	}

	public GameConfig getGameConfig(String gameId) {
		return gameConfigs.get(gameId);
	}

	public void putGameConfig(String gameId, GameConfig gameConfig) {
		gameConfigs.put(gameId, gameConfig);
	}

	public Room getRoom(Session session) {
		for (Room room : rooms) {
			if (room.getSessionIds().contains(session.getId())) {
				return room;
			}
		}
		return null;
	}

	public void removeSessionFromRoom(Session session) {
		Room occupiedRoom = null;
		for (Room room : rooms) {
			if (room.hasSession(session)) {
				occupiedRoom = room;
			}
		}
		if (occupiedRoom != null) {
			Logger.getLogger(getClass().getSimpleName()).info("Removing session from occupied room");
			rooms.remove(occupiedRoom);
			occupiedRoom.removePlayer(session);
			rooms.add(occupiedRoom);
		}
	}

	public void removeSessionFromAvailableRoom(Session session) {
		String matchMakeKey = null;
		Room availableRoom = null;
		for (Map.Entry<String, List<Room>> entry : availableRooms.entrySet()) {
			for (Room room : entry.getValue()) {
				if (room.hasSession(session)) {
					matchMakeKey = entry.getKey();
					availableRoom = room;
				}
			}
		}
		if (availableRoom != null) {
			Logger.getLogger(getClass().getSimpleName()).info("Removing session from available room");
			availableRooms.get(matchMakeKey).remove(availableRoom);
			availableRoom.removeAvailablePlayer(session);
			// check if available room is empty. if not, add back to availableRooms
			if(availableRoom.getPlayers().size() > 0) {
				availableRooms.get(matchMakeKey).add(availableRoom);
			}
		}
	}

	public List<Room> getAvailableRooms(String key) {
		synchronized (availableRooms) {
			if (availableRooms.containsKey(key)) {
				List<Room> rooms = availableRooms.get(key);
				if (rooms.size() > 0) {
					return rooms;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public Room addPlayerToAvailableRoom(String gameId, String key, Player player) throws Exception {
		synchronized (availableRooms) {
			List<Room> rooms = availableRooms.get(key);
			Room room = null;
			if (rooms == null) {
				rooms = new ArrayList<Room>();
				availableRooms.put(key, rooms);
			}
			if (rooms.size() < 1) {
				Room newRoom = new Room(Room.getRandomRoomId(), key, player, getGameConfig(gameId).getMaxAllowedPlayers());
				rooms.add(newRoom);
				return null;
			}
			for (Room availableRoom : rooms) {
				if (availableRoom.getPlayers().size() <= getGameConfig(gameId).getMinPlayersToStart()) {
					room = availableRoom;
					break;
				}
			}
			if (room != null) {
				rooms.remove(room);
				room.addPlayer(player);
				if (room.getPlayers().size() >= getGameConfig(gameId).getMinPlayersToStart()) {
					this.rooms.add(room);
					return room;
				}
				rooms.add(room);
			}

			return null;
		}
	}

	public Room getAvailableRoom(String key, Player player) {
		synchronized (availableRooms) {

			List<Room> rooms = availableRooms.get(key);
			for (Room room : rooms) {
				if (room.hasSession(player.getSession())) {
					return room;
				}
			}
			return null;
		}
	}

	public void addAvailableRoom(String key, Room room) {
		if (availableRooms.containsKey(key)) {
			List<Room> rooms = availableRooms.get(key);
			rooms.add(room);
			availableRooms.put(key, rooms);
		} else {
			List<Room> rooms = new ArrayList<Room>();
			rooms.add(room);
			availableRooms.put(key, rooms);
		}
	}

	public Room getRoom(String roomId) {
		for (Room room : rooms) {
			if (room.getRoomId().equals(roomId)) {
				return room;
			}
		}
		return null;
	}

	public void addRoom(Room room) {
		rooms.add(room);
	}

	public void removeRoom(Room room) {
		rooms.remove(room);
	}

	public static RealtimeData getRealtimeData() {
		if (instance == null) {
			return instance = new RealtimeData();
		}
		return instance;
	}

	private RealtimeData() {
		rooms = new ArrayList<Room>();
		availableRooms = new HashMap<String, List<Room>>();
		gameConfigs = new HashMap<String, GameConfig>();
	}

	public class RoomComparator implements Comparator<Room> {
		@Override
		public int compare(Room o1, Room o2) {
			return o1.getPlayers().size() - o2.getPlayers().size();
		}
	}
}
