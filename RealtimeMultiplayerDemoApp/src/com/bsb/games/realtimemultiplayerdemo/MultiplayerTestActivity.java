package com.bsb.games.realtimemultiplayerdemo;

import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.bsb.games.multiplayer.RealtimeMultiplayerClient;
import com.bsb.games.multiplayer.RealtimeMultiplayerClient.RealtimeMultiplayerEvents;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;
import com.bsb.games.multiplayer.response.RoomResponse;

public class MultiplayerTestActivity extends Activity implements OnClickListener {
	private String TAG = getClass().getSimpleName();
	private EditText input;
	private RealtimeMultiplayerClient client;
	private PlayerDetails player = new PlayerDetails();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_activity);
		Log.d(TAG, "Opening MultiplayerTestActivity");

		findViewById(R.id.createRoomButton).setOnClickListener(this);
		findViewById(R.id.joinRoomButton).setOnClickListener(this);
		findViewById(R.id.leaveRoomButton).setOnClickListener(this);
		findViewById(R.id.sendMessageButton).setOnClickListener(this);
		findViewById(R.id.matchMakeButton).setOnClickListener(this);
		findViewById(R.id.chatButton).setOnClickListener(this);
		findViewById(R.id.disconnectButton).setOnClickListener(this);

		player.id = UUID.randomUUID().toString();
		player.name = "Susheel";
		try {
			client = new RealtimeMultiplayerClient(this, "99", player, new RealtimeMultiplayerEvents() {

				@Override
				public void onRoomUpdated(String roomId, final List<PlayerDetails> players) {
					showToastMessage("Room Updated. Player size : " + players.size() + ", RoomId : " + roomId);
				}

				@Override
				public void onPlayerLeaveRoom(String roomId, final PlayerDetails player) {
					showToastMessage(player.name + " left the room");
				}

				@Override
				public void onMessage(final PlayerDetails player, final byte[] data) {
					showToastMessage("New Message from " + player.name + ", message : " + new String(data));
				}

				@Override
				public void onMatchMakingDone(final String roomId, final List<PlayerDetails> players) {
					showToastMessage("Matchmaking Done. RoomId : " + roomId + ", Total Players : " + players.size());
				}

				@Override
				public void onDisconnected() {
					showToastMessage("Disconnected");
				}

				@Override
				public void onConnected() {
					showToastMessage("Connected");
				}

				@Override
				public void onChatMessage(PlayerDetails player, byte[] data) {

				}
				
				@Override
				public void onError(MultiplayerActionType type) {
					Toast.makeText(getApplicationContext(), "Error in : "+type, Toast.LENGTH_SHORT).show();
				}
			});
			client.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showToastMessage(String message) {
		Log.d(TAG, "Toast : " + message);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View view) {
		final RoomResponse response = new RoomResponse();
		response.playerDetails.id = "BSB|123432123421";
		response.playerDetails.name = "Susheel";
		try {
			switch (view.getId()) {
			case R.id.createRoomButton:
				client.createRoom("test");
				break;
			case R.id.joinRoomButton:
				showPromptDialog("Multiplayer", "Please enter the room  id", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							client.joinRoom(input.getEditableText().toString());
						} catch (Exception e) {
							showToastMessage(e.getMessage());
						}
					}
				});
				break;
			case R.id.leaveRoomButton:
				client.leaveRoom();
				break;
			case R.id.sendMessageButton:
				showPromptDialog("Multiplayer", "Please enter the message to send", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							client.sendMessage(input.getEditableText().toString().getBytes());
						} catch (Exception e) {
							showToastMessage(e.getMessage());
						}
					}
				});
				break;
			case R.id.matchMakeButton:
				client.matchMake("test");
				break;
			case R.id.chatButton:
				client.openChatRoom();
				break;
			case R.id.disconnectButton:
				client.disconnect();
				break;
			}
		} catch (Exception e) {
			showToastMessage(e.getMessage());
		}
	}

	private void showPromptDialog(String title, String message, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);
		alert.setMessage(message);

		// Set an EditText view to get user input
		input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", listener);

		alert.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (client != null && client.isConnected()) {
			client.disconnect();
		}
	}

}