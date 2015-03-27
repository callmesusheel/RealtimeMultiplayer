package com.bsb.games.multiplayer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.bsb.games.multiplayer.RealtimeMultiplayerClient.RealtimeMultiplayerEvents;
import com.bsb.games.multiplayer.WebSocketClient.Listener;
import com.bsb.games.multiplayer.actiondata.ActionRequest;
import com.bsb.games.multiplayer.actiondata.CreateRoomRequest;
import com.bsb.games.multiplayer.actiondata.DisconnectRequest;
import com.bsb.games.multiplayer.actiondata.ExitRoomRequest;
import com.bsb.games.multiplayer.actiondata.JoinRoomRequest;
import com.bsb.games.multiplayer.actiondata.MatchRoomRequest;
import com.bsb.games.multiplayer.actiondata.PingRequest;
import com.bsb.games.multiplayer.actiondata.SendMessageRequest;
import com.bsb.games.multiplayer.bots.NameGenerator;
import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.response.ActionResponse;
import com.bsb.games.multiplayer.response.CreateRoomResponse;
import com.bsb.games.multiplayer.response.ExitRoomResponse;
import com.bsb.games.multiplayer.response.JoinRoomResponse;
import com.bsb.games.multiplayer.response.MatchRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.response.SendMessageResponse;
import com.google.gson.Gson;

public class RealtimeMultiplayerClient2 {

	private Activity activity;
	private RealtimeMultiplayerEvents callback;
	private WebSocketClient client;
	private PlayerDetails player;
	private String TAG = getClass().getCanonicalName();
	private boolean isConnected;
	private String roomId;
	private String gameId;
	private List<PlayerBot> botPlayers;
	private int botMatchDelay = 0;
	private boolean isBotPlaying = false;
	private List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
	private final long pingInterval = 30000; // 1/2 minute
	private long lastPingResponse;
	private ChatDialog dialog;
	private List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
	
	private String getRoomId() {
		return roomId;
	}
	
	private void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	private enum ConnectActionType {
		MATCHMAKE,CREATEROOM,JOINROOM,
	}
	
	public boolean isConnected() {
		return isConnected;
	}

	public RealtimeMultiplayerClient2(Activity activity, String gameId, RealtimeMultiplayerEvents callback) {
		this.activity = activity;
		this.callback = callback;
		this.gameId = gameId;
	}

	public RealtimeMultiplayerClient2(Activity activity, String gameId, PlayerDetails player, List<PlayerBot> botPlayers, int botMatchDelay,
			int minPlayersNeeded, RealtimeMultiplayerEvents callback) {
		this.activity = activity;
		this.callback = callback;
		this.player = player;
		this.gameId = gameId;
		this.botPlayers = botPlayers;
		this.botMatchDelay = botMatchDelay;
	}

	public void setPlayerDetails(String name, String id, Map<String, String> moreDetails) {
		this.player = new PlayerDetails();
		this.player.id = id;
		this.player.name = name;
		this.player.properties = moreDetails;
	}

	private void connectAction(final ConnectActionType connectActionType,final String extraParam) {
		Log.d(TAG,"connectAction : "+connectActionType);
		client = new WebSocketClient(URI.create(activity.getResources().getString(getResourceIdByName(activity, "string", "server_link"))),
				new Listener() {

					@Override
					public void onMessage(byte[] data) {
						Log.d(TAG,"onMessage and data in byte[]");
						String response = new String(data);
						onMessage(response);
					}

					@Override
					public void onMessage(String message) {

						Log.d(TAG, "Message : " + message);
						final ActionResponse response = new Gson().fromJson(message, ActionResponse.class);
						switch (response.action) {
						case CREATE_ROOM:
							final CreateRoomResponse createRoomResponse = new Gson().fromJson(message, CreateRoomResponse.class);
							if (createRoomResponse.status.success) {
								setRoomId(createRoomResponse.roomId);
								final List<PlayerDetails> roomPlayerDetails = new ArrayList<PlayerDetails>();
								roomPlayerDetails.add(createRoomResponse.user);
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										callback.onRoomUpdated(createRoomResponse.roomId, roomPlayerDetails);
									}
								});
							} else {
								sendErrorCallback(MultiplayerActionType.CREATE_ROOM);
							}
							break;
						case EXIT_ROOM:
							final ExitRoomResponse exitRoomResponse = new Gson().fromJson(message, ExitRoomResponse.class);
							if (exitRoomResponse.payload.leavingPlayer.id.equals(player.id)) {
								setRoomId(null);
							}
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onPlayerLeaveRoom(exitRoomResponse.payload.roomId, exitRoomResponse.payload.leavingPlayer);
								}
							});
							break;
						case JOIN_ROOM:
							final JoinRoomResponse joinRoomResponse = new Gson().fromJson(message, JoinRoomResponse.class);
							if (joinRoomResponse.status.success) {
								setRoomId(joinRoomResponse.payload.roomId);
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										callback.onRoomUpdated(joinRoomResponse.payload.roomId, joinRoomResponse.payload.participants);
									}
								});
							} else {
								sendErrorCallback(MultiplayerActionType.JOIN_ROOM);
							}
							break;
						case MATCH_MAKE:
							final MatchRoomResponse matchRoomResponse = new Gson().fromJson(message, MatchRoomResponse.class);
							if (matchRoomResponse.status.success) {
								setRoomId(matchRoomResponse.payload.roomId);
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										callback.onMatchMakingDone(matchRoomResponse.payload.roomId, matchRoomResponse.payload.participants);
									}
								});
							} else {
								sendErrorCallback(MultiplayerActionType.MATCH_MAKE);
							}
							break;
						case PING:
							lastPingResponse = System.currentTimeMillis();
							break;
						case SEND_MESSAGE:
							final SendMessageResponse sendMessageResponse = new Gson().fromJson(message, SendMessageResponse.class);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (sendMessageResponse.payload.messageType.equals("CHAT")) {
										chatMessages.add(new ChatMessage(sendMessageResponse.payload.sender, new String(
												sendMessageResponse.payload.data)));
										if (dialog != null && dialog.isShowing()) {
//											dialog.setChatMessages(chatMessages);
										}
										callback.onChatMessage(sendMessageResponse.payload.sender, sendMessageResponse.payload.data);
									} else {
										callback.onMessage(sendMessageResponse.payload.sender, sendMessageResponse.payload.data);
									}
								}
							});
							break;
						case DISCONNECT:
							client.disconnect();
							break;
						default:
							break;
						}
					
					}

					@Override
					public void onError(final Exception error) {
						Log.d(TAG, "Exception : " + error.getMessage());
						Log.d(TAG, "RoomId : " + getRoomId());
						if (getRoomId() != null && getRoomId().equals("botRoom")) {
							Log.d(TAG, "onError() returning from here");
							return;
						}
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (error != null && error.getMessage() != null && error.getMessage().contains("ECONNREFUSED")) {
									if (!isConnected) { // return if not
														// connected
										return;
									}
									callback.onDisconnected();
								}
							}
						});
					}

					@Override
					public void onDisconnect(int code, String reason) {
						if (getRoomId() != null && getRoomId().equals("botRoom")) {
							Log.d(TAG, "onDisconnect() returning from here");
							return;
						}
						isConnected = false;
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								callback.onDisconnected();
							}
						});
					}

					@Override
					public void onConnect() {
						isConnected = true;
						handler.postDelayed(runable, pingInterval);
						lastPingResponse = System.currentTimeMillis();
						switch (connectActionType) {
						case CREATEROOM:
							try {
								createRoomAction(extraParam);
							} catch (Exception e1) {
								e1.printStackTrace();
								sendErrorCallback(MultiplayerActionType.CREATE_ROOM);
							}
							break;
						case JOINROOM:
							try {
								joinRoomAction(extraParam);
							} catch (Exception e1) {
								e1.printStackTrace();
								sendErrorCallback(MultiplayerActionType.CREATE_ROOM);
							}
							break;
						case MATCHMAKE:
							try {
								matchMakeAction(extraParam);
							} catch (Exception e3) {
								e3.printStackTrace();
								sendErrorCallback(MultiplayerActionType.MATCH_MAKE);
							}
							break;
						}
					}
				}, headers);
		
		client.connect();
	}
	
	public void matchMake(String key) {
		connectAction(ConnectActionType.MATCHMAKE, key);
	}
	
	public void joinRoom(String joinRoomId) {
		Log.d(TAG,"RoomId : "+joinRoomId);
		connectAction(ConnectActionType.JOINROOM, joinRoomId);
		for(String str : joinRoomId.split("\\|")) {
			Log.d(TAG,"Str : "+str);
		}
		String hostId = joinRoomId.split("\\|")[0];
		Log.d(TAG,"Header added : "+hostId);
		headers.add(new BasicNameValuePair("hostAddress", hostId));
	}
	
	public void createRoom(String key) {
		connectAction(ConnectActionType.CREATEROOM, key);
	}
	
	private void joinRoomAction(String joiningRoomId) throws Exception {
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				JoinRoomRequest joinRoomRequest = new JoinRoomRequest();
				joinRoomRequest.user = player;
				joinRoomRequest.type = MultiplayerActionType.JOIN_ROOM;
				joinRoomRequest.gameId = gameId;
				joinRoomRequest.roomId = joiningRoomId;
				ActionRequest request = new ActionRequest();
				request.data = joinRoomRequest;
				client.send(new Gson().toJson(request));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createRoomAction(String key) throws Exception {
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				CreateRoomRequest createRoomRequest = new CreateRoomRequest();
				createRoomRequest.type = MultiplayerActionType.CREATE_ROOM;
				createRoomRequest.gameId = gameId;
				createRoomRequest.filter = key;
				createRoomRequest.user = player;
				ActionRequest request = new ActionRequest();
				request.data = createRoomRequest;
				client.send(new Gson().toJson(request));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void matchMakeAction(String key) throws Exception {
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				MatchRoomRequest matchRoomRequest = new MatchRoomRequest();
				matchRoomRequest.type = MultiplayerActionType.MATCH_MAKE;
				matchRoomRequest.filter = key;
				matchRoomRequest.user = player;
				matchRoomRequest.gameId = gameId;

				ActionRequest request = new ActionRequest();
				request.data = matchRoomRequest;
				client.send(new Gson().toJson(request));
				// if botMatchDelay is 0, then bot related constructor is not
				// being used. So returning
				if (botMatchDelay == 0) {
					return;
				}

				new java.util.Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						// Still not part of a room, so match the player with a
						// bot
						if (getRoomId() == null && isConnected) {
							try {
								Log.d(TAG, "Setting roomId = botRoom");
								setRoomId("botRoom");
								Log.d(TAG, "RoomId : " + getRoomId());
								for (PlayerBot botPlayer : botPlayers) {
									botPlayer.getBotPlayer().id = UUID.randomUUID().toString();
									botPlayer.getBotPlayer().name = NameGenerator.getCoolNickName();
									botPlayer.setCallback(callback);
									botPlayer.setActivity(activity);
								}
								disconnect();
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										isBotPlaying = true;
										callback.onMatchMakingDone("botRoom", getBotsAsPlayers());
										for (PlayerBot botPlayer : botPlayers) {
											botPlayer.onMatchMakingDone("botRoom", getBotsAsPlayers());
										}
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}, botMatchDelay * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		Log.d(TAG, "disconnect()");
		try {
			if (isBotPlaying) {
				for (PlayerBot botPlayer : botPlayers) {
					botPlayer.onPlayerLeaveRoom(getRoomId(), player);
				}
				removeBotRoom();
				return;
			}
			DisconnectRequest disconnectRequest = new DisconnectRequest();
			disconnectRequest.type = MultiplayerActionType.DISCONNECT;
			disconnectRequest.gameId = gameId;
			ActionRequest request = new ActionRequest();
			request.data = disconnectRequest;
			client.send(new Gson().toJson(request));
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getRoomId() != null && getRoomId().equals("botRoom")) {
			// do nothing
		} else {
			setRoomId(null);
		}
	}

	public void sendMessage(byte[] data) throws Exception {
		try {
			if (isBotPlaying) {
				for (PlayerBot bot : botPlayers) {
					bot.onMessage(player, data);
				}
				return;
			}
			if (getRoomId() != null && !getRoomId().equals("")) {
				SendMessageRequest sendMessageRequest = new SendMessageRequest();
				sendMessageRequest.user = player;
				sendMessageRequest.data = data;
				sendMessageRequest.type = MultiplayerActionType.SEND_MESSAGE;
				sendMessageRequest.gameId = gameId;
				sendMessageRequest.messageType = "GAME";
				sendMessageRequest.roomId = getRoomId();
				ActionRequest request = new ActionRequest();
				request.data = sendMessageRequest;
				client.send(new Gson().toJson(request));
			} else {
				Log.d(TAG, "Player is not part of any room to send a message");
				throw new Exception("Player is not part of any room to send a message");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openChatRoom() throws Exception {
		if (getRoomId() != null && !getRoomId().equals("")) {
			dialog = new ChatDialog(activity, this, player, chatMessages);
			dialog.show();
		} else {
			Log.d(TAG, "Player is not part of any room to open chat message");
			throw new Exception("Player is not part of any room to open chat message");
		}
	}

	public void sendChatMessage(String data) throws Exception {
		try {
			if (isBotPlaying) {
				// nothing to do. Bots dont talk
				return;
			}
			if (getRoomId() != null && !getRoomId().equals("")) {
				SendMessageRequest sendMessageRequest = new SendMessageRequest();
				sendMessageRequest.user = player;
				sendMessageRequest.data = data.getBytes();
				sendMessageRequest.type = MultiplayerActionType.SEND_MESSAGE;
				sendMessageRequest.gameId = gameId;
				sendMessageRequest.messageType = "CHAT";
				sendMessageRequest.roomId = getRoomId();
				ActionRequest request = new ActionRequest();
				request.data = sendMessageRequest;

				chatMessages.add(new ChatMessage(player, data));
				if (dialog != null && dialog.isShowing()) {
					dialog.setChatMessages(chatMessages);
				}
				client.send(new Gson().toJson(request));
			} else {
				Log.d(TAG, "Player is not part of any room to send a message");
				throw new Exception("Player is not part of any room to send a message");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void leaveRoom() throws Exception {
		try {
			if (isBotPlaying) {
				for (PlayerBot botPlayer : botPlayers) {
					botPlayer.onPlayerLeaveRoom(getRoomId(), player);
				}
				removeBotRoom();
				return;
			}
			if (getRoomId() != null && !getRoomId().equals("")) {
				ExitRoomRequest exitRoomRequest = new ExitRoomRequest();
				exitRoomRequest.type = MultiplayerActionType.EXIT_ROOM;
				exitRoomRequest.roomId = getRoomId();
				exitRoomRequest.user = player;
				exitRoomRequest.gameId = gameId;
				ActionRequest request = new ActionRequest();
				request.data = exitRoomRequest;
				client.send(new Gson().toJson(request));
			} else {
				Log.d(TAG, "Player is not part of any room to exit");
				throw new Exception("Player is not part of any room to exit");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setRoomId(null);
	}
	
	final Handler handler = new Handler();
	Runnable runable = new Runnable() {

		@Override
		public void run() {
			try {
				if (isConnected()) {
					if (System.currentTimeMillis() - lastPingResponse > (2 * pingInterval)) {
						client.disconnect();
						return;
					}
					Log.d(TAG,"sending ping message");
					PingRequest pingRequest = new PingRequest();
					pingRequest.type = MultiplayerActionType.PING;
					ActionRequest request = new ActionRequest();
					request.data = pingRequest;
					client.send(new Gson().toJson(request));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// also call the same runnable
				handler.postDelayed(this, pingInterval);
			}
		}
	};
	
	private List<PlayerDetails> getBotsAsPlayers() {
		List<PlayerDetails> playerDetailsList = new ArrayList<PlayerDetails>();
		for (PlayerBot bot : botPlayers) {
			playerDetailsList.add(bot.getBotPlayer());
		}
		playerDetailsList.add(player);
		return playerDetailsList;
	}

	private void removeBotRoom() {
		Log.d(TAG, "Removing bot room");
		setRoomId(null);
		isBotPlaying = false;
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				callback.onDisconnected();	
			}
		});
	}

	public static int getResourceIdByName(Context context, String className, String name) {
		String packageName = context.getPackageName();
		Resources res = context.getResources();
		int resource = res.getIdentifier(name, className, packageName);
		return resource;
	}
	
	private void sendErrorCallback(final MultiplayerActionType type) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				callback.onError(type);
			}
		});
	}
	
}
