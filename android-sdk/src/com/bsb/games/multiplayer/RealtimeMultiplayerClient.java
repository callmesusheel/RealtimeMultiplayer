package com.bsb.games.multiplayer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.response.CreateRoomResponse;
import com.bsb.games.multiplayer.response.DisconnectResponse;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.bsb.games.multiplayer.response.ExitRoomResponse;
import com.bsb.games.multiplayer.response.JoinRoomResponse;
import com.bsb.games.multiplayer.response.MatchRoomResponse;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.response.RoomResponse;
import com.bsb.games.multiplayer.response.SendMessageResponse;
import com.google.gson.Gson;

public class RealtimeMultiplayerClient {

	public interface RealtimeMultiplayerEvents {

		public void onMessage(PlayerDetails player, byte[] data);

		public void onChatMessage(PlayerDetails player, byte[] data);

		public void onMatchMakingDone(String roomId, List<PlayerDetails> players);

		public void onPlayerLeaveRoom(String roomId, PlayerDetails player);

		public void onRoomUpdated(String roomId, List<PlayerDetails> players);

		public void onConnected();

		public void onDisconnected();

		public void onError(MultiplayerActionType type);
	}

	private Activity activity;
	private RealtimeMultiplayerEvents callback;
	private WebSocketClient client;
	private PlayerDetails player;
	private String TAG = getClass().getCanonicalName();
	private boolean isConnected;
	private String roomId;
	private String gameId;
	private String matchMakingKey;

	private List<PlayerBot> botPlayers;
	private int botMatchDelay = 0;

	private boolean isBotPlaying = false;

	private List<ChatMessage> chatMessages = new ArrayList<RealtimeMultiplayerClient.ChatMessage>();

	private final long pingInterval = 60000; // 1 minute

	private long lastPingResponse;
	private ChatDialog dialog;

	public boolean isConnected() {
		return isConnected;
	}

	private void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	private String getRoomId() {
		return roomId;
	}

	public RealtimeMultiplayerClient(Activity activity, String gameId, PlayerDetails player, RealtimeMultiplayerEvents callback) {
		this.activity = activity;
		this.callback = callback;
		this.player = player;
		this.gameId = gameId;
		init();
	}

	public RealtimeMultiplayerClient(Activity activity, String gameId, PlayerDetails player, List<PlayerBot> botPlayers, int botMatchDelay,
			int minPlayersNeeded, RealtimeMultiplayerEvents callback) {
		this.activity = activity;
		this.callback = callback;
		this.player = player;
		this.gameId = gameId;
		this.botPlayers = botPlayers;
		this.botMatchDelay = botMatchDelay;
		init();
	}

	public void setPlayerDetails(String name, String id, Map<String, String> moreDetails) {
		this.player = new PlayerDetails();
		this.player.id = id;
		this.player.name = name;
		this.player.moreDetails = moreDetails;
	}

	public void init() {
		client = new WebSocketClient(URI.create(activity.getResources().getString(getResourceIdByName(activity, "string", "server_link"))),
				new WebSocketClient.Listener() {

					@Override
					public void onConnect() {
						Log.d(TAG, "Connected");
						isConnected = true;
						handler.postDelayed(runable, pingInterval);
						lastPingResponse = System.currentTimeMillis();
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								callback.onConnected();
							}
						});
					}

					@Override
					public void onMessage(String message) {
						Log.d(TAG, "Message : " + message);
						final SendMessageResponse response = new Gson().fromJson(message, SendMessageResponse.class);
						switch (response.action) {
						case CHATMESSAGE:
							final SendMessageResponse chatMessageResponse = new Gson().fromJson(message, SendMessageResponse.class);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									chatMessages.add(new ChatMessage(chatMessageResponse.playerDetails, new String(chatMessageResponse.data)));
									if (dialog != null && dialog.isShowing()) {
										dialog.setChatMessages(chatMessages);
									}
									callback.onChatMessage(chatMessageResponse.playerDetails, response.data);
								}
							});
							break;
						case CREATEROOM:
							final CreateRoomResponse createRoomResponse = new Gson().fromJson(message, CreateRoomResponse.class);
							setRoomId(createRoomResponse.roomId);
							final List<PlayerDetails> roomPlayerDetails = new ArrayList<PlayerDetails>();
							roomPlayerDetails.add(createRoomResponse.playerDetails);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onRoomUpdated(createRoomResponse.roomId, roomPlayerDetails);
								}
							});
							break;
						case EXITROOM:
							final ExitRoomResponse exitRoomResponse = new Gson().fromJson(message, ExitRoomResponse.class);
							if (exitRoomResponse.playerDetails.id.equals(player.id)) {
								setRoomId(null);
							}
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onPlayerLeaveRoom(exitRoomResponse.roomId, exitRoomResponse.playerDetails);
								}
							});
							break;
						case JOINROOM:
							final JoinRoomResponse joinRoomResponse = new Gson().fromJson(message, JoinRoomResponse.class);
							setRoomId(joinRoomResponse.roomId);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onRoomUpdated(joinRoomResponse.roomId, joinRoomResponse.roomPlayers);
								}
							});
							break;
						case MATCHMAKE:
							final MatchRoomResponse matchRoomResponse = new Gson().fromJson(message, MatchRoomResponse.class);
							setRoomId(matchRoomResponse.roomId);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onMatchMakingDone(matchRoomResponse.roomId, matchRoomResponse.roomPlayers);
								}
							});
							break;
						case PING:
							lastPingResponse = System.currentTimeMillis();
							break;
						case SENDMESSAGE:
							final SendMessageResponse sendMessageResponse = new Gson().fromJson(message, SendMessageResponse.class);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									callback.onMessage(sendMessageResponse.playerDetails, response.data);
								}
							});
							break;
						case DISCONNECT:
							client.disconnect();
							break;
						case ERROR:
							final ErrorResponse errorResponse = new Gson().fromJson(message, ErrorResponse.class);
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (errorResponse != null) {
										callback.onError(errorResponse.error);
									}
								}
							});
							break;
						}
					}

					@Override
					public void onMessage(byte[] data) {
						Log.d(TAG, "Message : " + new String(data));

					}

					@Override
					public void onDisconnect(int code, String reason) {
						Log.d(TAG, "Disconnected : " + code + ". Reason : " + reason);
						Log.d(TAG, "RoomId : " + getRoomId());
						if (getRoomId() != null && getRoomId().equals("botRoom")) {
							Log.d(TAG, "onDisconnect() returning from here");
							return;
						}
						isConnected = false;
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Log.d(TAG, "onDisconnect() calling callback.onDisconnected()");
								callback.onDisconnected();
							}
						});
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
								if (error.getMessage().contains("ECONNREFUSED")) {
									Log.d(TAG, "onError() calling callback.onDisconnected()");
									callback.onDisconnected();
								}
							}
						});
					}

				}, null);
	}

	public void matchMake(String key) throws Exception {
		this.matchMakingKey = key;
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				MatchRoomResponse matchRoomResponse = new MatchRoomResponse();
				matchRoomResponse.action = MultiplayerActionType.MATCHMAKE;
				matchRoomResponse.matchMakeKey = key;
				matchRoomResponse.playerDetails = player;
				matchRoomResponse.gameId = gameId;
				client.send(new Gson().toJson(matchRoomResponse));
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
						if (getRoomId() == null) {
							try {

								Log.d(TAG, "Setting roomId = botRoom");
								setRoomId("botRoom");
								Log.d(TAG, "RoomId : " + getRoomId());
								for (PlayerBot botPlayer : botPlayers) {
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

	public void connect() {
		client.connect();
	}

	public void disconnect() {
		try {
			if (isBotPlaying) {
				removeBotRoom();
				return;
			}
			Log.d(TAG, "disconnect()");
			DisconnectResponse disconnectResponse = new DisconnectResponse();
			disconnectResponse.action = MultiplayerActionType.DISCONNECT;
			disconnectResponse.roomId = getRoomId();
			disconnectResponse.matchMakeKey = matchMakingKey;
			disconnectResponse.playerDetails = player;
			disconnectResponse.gameId = gameId;
			client.send(new Gson().toJson(disconnectResponse));
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
				SendMessageResponse sendMessageResponse = new SendMessageResponse();
				sendMessageResponse.playerDetails = player;
				sendMessageResponse.data = data;
				sendMessageResponse.action = MultiplayerActionType.SENDMESSAGE;
				sendMessageResponse.gameId = gameId;
				sendMessageResponse.roomId = getRoomId();
				client.send(new Gson().toJson(sendMessageResponse));
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
				SendMessageResponse sendMessageResponse = new SendMessageResponse();
				sendMessageResponse.playerDetails = player;
				sendMessageResponse.data = data.getBytes();
				sendMessageResponse.action = MultiplayerActionType.CHATMESSAGE;
				sendMessageResponse.gameId = gameId;
				sendMessageResponse.roomId = getRoomId();
				chatMessages.add(new ChatMessage(player, data));
				if (dialog != null && dialog.isShowing()) {
					dialog.setChatMessages(chatMessages);
				}
				client.send(new Gson().toJson(sendMessageResponse));
			} else {
				Log.d(TAG, "Player is not part of any room to send a message");
				throw new Exception("Player is not part of any room to send a message");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void joinRoom(String joinindRoomId) throws Exception {
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				JoinRoomResponse joinRoomResponse = new JoinRoomResponse();
				joinRoomResponse.playerDetails = player;
				joinRoomResponse.action = MultiplayerActionType.JOINROOM;
				joinRoomResponse.gameId = gameId;
				joinRoomResponse.roomId = joinindRoomId;
				client.send(new Gson().toJson(joinRoomResponse));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createRoom(String key) throws Exception {
		try {
			if (getRoomId() != null && !getRoomId().equals("")) {
				Log.d(TAG, "Player is already part of a room");
				throw new Exception("Player is already part of a room");
			} else {
				CreateRoomResponse createRoomResponse = new CreateRoomResponse();
				createRoomResponse.action = MultiplayerActionType.CREATEROOM;
				createRoomResponse.gameId = gameId;
				createRoomResponse.matchKey = key;
				createRoomResponse.playerDetails = player;
				client.send(new Gson().toJson(createRoomResponse));
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
				ExitRoomResponse exitRoomResponse = new ExitRoomResponse();
				exitRoomResponse.action = MultiplayerActionType.EXITROOM;
				exitRoomResponse.roomId = getRoomId();
				exitRoomResponse.playerDetails = player;
				exitRoomResponse.gameId = gameId;
				client.send(new Gson().toJson(exitRoomResponse));
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
					RoomResponse response = new RoomResponse();
					response.action = MultiplayerActionType.PING;
					response.gameId = gameId;
					response.playerDetails = player;
					client.send(new Gson().toJson(response));
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
		botPlayers = null;
		botMatchDelay = 0;
		isBotPlaying = false;
		callback.onDisconnected();
	}

	public static int getResourceIdByName(Context context, String className, String name) {
		String packageName = context.getPackageName();
		Resources res = context.getResources();
		int resource = res.getIdentifier(name, className, packageName);
		return resource;
	}

	class ChatMessage {
		PlayerDetails player;
		String message;

		ChatMessage(PlayerDetails player, String message) {
			this.player = player;
			this.message = message;
		}
	}

}
