package com.bsb.games.multiplayer.unitywrapper;

import java.util.List;

import com.bsb.games.multiplayer.RealtimeMultiplayerClient;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.RealtimeMultiplayerClient.RealtimeMultiplayerEvents;
import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class BSBMultiplayer {

	private static Activity GameActivity;
	private static String gameId;
	private static RealtimeMultiplayerClient MultiplayerClient;
	private static PlayerDetails Player;
	private static Boolean isInitialized = false;
	private static String UnityObjectName = "BSBMultiplayer";
	
	static void init(final String gameId, final String name, final String id){
		GameActivity = com.unity3d.player.UnityPlayer.currentActivity;
		BSBMultiplayer.gameId = gameId;
		Player = new PlayerDetails();
		Player.name = name;
		Player.id = id;
		
		GameActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MultiplayerClient = new RealtimeMultiplayerClient(GameActivity, BSBMultiplayer.gameId, Player, new RealtimeMultiplayerEvents() {
					
					@Override
					public void onRoomUpdated(String arg0, List<PlayerDetails> arg1) {
						// TODO Auto-generated method stub				
					}
					
					@Override
					public void onPlayerLeaveRoom(String roomId, PlayerDetails arg1) {
						// TODO Auto-generated method stub
						UnityPlayer.UnitySendMessage(UnityObjectName, "onPlayerLeaveRoom", new Gson().toJson(arg1));
						Toast.makeText(GameActivity, "Player Left Room: " + arg1.name, Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onMessage(PlayerDetails arg0, byte[] arg1) {
						// TODO Auto-generated method stub
						String dataString = new String(arg1);
						UnityPlayer.UnitySendMessage(UnityObjectName, "onMessage", dataString);
					}
					
					@Override
					public void onMatchMakingDone(String arg0, List<PlayerDetails> arg1) {
						// TODO Auto-generated method stub
						UnityPlayer.UnitySendMessage(UnityObjectName, "onMatchMakingDone", new Gson().toJson(arg1));
					}
					
					@Override
					public void onDisconnected() {
						// TODO Auto-generated method stub
						UnityPlayer.UnitySendMessage(UnityObjectName, "onDisconnected", "");
					}
					
					@Override
					public void onConnected() {
						Log.d(UnityObjectName, "Java:onConnected");
						// TODO Auto-generated method stub
						UnityPlayer.UnitySendMessage(UnityObjectName, "onConnected", "");
					}

					@Override
					public void onChatMessage(PlayerDetails player, byte[] data) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onError(MultiplayerActionType type) {
						if(type.equals(MultiplayerActionType.MATCH_MAKE)) {
							UnityPlayer.UnitySendMessage(UnityObjectName, "onDisconnected", "");
							Toast.makeText(GameActivity, "No Players online now. Try again", Toast.LENGTH_SHORT).show();
						}
						
					}
				});				
			}
		});

		isInitialized = true;
	}
	
	public static void clientConnect(){
		GameActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MultiplayerClient.connect();		
			}
		});		
	}
	
	public static void clientDisconnect(){	
		GameActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MultiplayerClient.disconnect();	
			}
		});
	}
	
	public static void matchMake(final String key){
		if(!isInitialized){
			Log.d("BSBMultiplayer", "Not Initialized");
			return;
		}
		
		GameActivity.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					MultiplayerClient.matchMake(key);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});		
	}
	
	public static void sendMessage(final String msg)
	{
		if(!isInitialized){
			return;
		}
		GameActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					MultiplayerClient.sendMessage(msg.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
