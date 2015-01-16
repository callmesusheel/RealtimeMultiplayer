package com.bsb.games.multiplayer.properties;

public class GameConfig {

	private int maxAllowedPlayers = 6;
	private long maxIdleTimeout = 60000;
	private int minPlayersToStart = 3;
	private Class<?> botClass;

	public GameConfig(int maxAllowedPlayers, int minPlayersToStart,long maxIdleTimeout,Class<?> botClass) {
		super();
		this.maxAllowedPlayers = maxAllowedPlayers;
		this.minPlayersToStart = minPlayersToStart;
		this.maxIdleTimeout = maxIdleTimeout;
		this.botClass = botClass;
	}
	
	public int getMinPlayersToStart() {
		return minPlayersToStart;
	}

	public void setMinPlayersToStart(int minPlayersToStart) {
		this.minPlayersToStart = minPlayersToStart;
	}

	public int getMaxAllowedPlayers() {
		return maxAllowedPlayers;
	}

	public void setMaxAllowedPlayers(int maxAllowedPlayers) {
		this.maxAllowedPlayers = maxAllowedPlayers;
	}

	public long getMaxIdleTimeout() {
		return maxIdleTimeout;
	}

	public void setMaxIdleTimeout(long maxIdleTimeout) {
		this.maxIdleTimeout = maxIdleTimeout;
	}
	
	public Class<?> getBotClass() {
		return botClass;
	}

	public void setBotClass(Class<?> botClass) {
		this.botClass = botClass;
	}

}
