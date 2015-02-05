package com.bsb.games.multiplayer;

import org.glassfish.tyrus.server.Server;

import com.bsb.games.multiplayer.bots.SDKTestAppBot;
import com.bsb.games.multiplayer.properties.GameConfig;
import com.bsb.games.multiplayer.properties.RealtimeData;

/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) {
		runServer();
	}

	public static void runServer() {
		Server server = new Server("localhost", 10024, "/mp", RealtimeApi.class);

		try {
			server.start();
			GameConfig gameConfig = new GameConfig(6, 2, 60000*3, SDKTestAppBot.class);
			RealtimeData.getRealtimeData().putGameConfig("99", gameConfig);
			System.out.println("Server Starting");
			Thread.currentThread().join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			server.stop();
		}

	}
}
