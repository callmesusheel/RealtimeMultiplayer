package com.bsb.games.realtimemultiplayerdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bsb.games.multiplayer.RealtimeMultiplayerClient;
import com.bsb.games.multiplayer.RealtimeMultiplayerClient.RealtimeMultiplayerEvents;
import com.bsb.games.multiplayer.bots.PlayerBot;
import com.bsb.games.multiplayer.bots.SDKTestAppBot;
import com.bsb.games.multiplayer.response.MultiplayerActionType;
import com.bsb.games.multiplayer.response.PlayerDetails;


public class MultiplayerDemoActivity extends Activity implements OnClickListener {
	private String TAG = getClass().getSimpleName();
	private EditText input;
	private RealtimeMultiplayerClient client;
	private PlayerDetails player = new PlayerDetails();
	private PlayerDetails opponent = new PlayerDetails();
	private TextView player1TextView;
	private TextView player2TextView;
	private Button mainButton;
	private boolean isGameOn;
	private ProgressDialog pdia;
	private int score = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_demo);
		Log.d(TAG, "Opening MultiplayerTestActivity");
		mainButton = (Button) findViewById(R.id.mainButton);
		player1TextView = (TextView) findViewById(R.id.player1);
		player2TextView = (TextView) findViewById(R.id.player2);
		mainButton.setOnClickListener(this);
		findViewById(R.id.chatButton).setOnClickListener(this);
		pdia = new ProgressDialog(this);
		pdia.setCancelable(false);

	}

	public void init() {
		try {
			List<PlayerBot>botPlayers = new ArrayList<PlayerBot>();
			SDKTestAppBot testBot = new SDKTestAppBot();
			botPlayers.add(testBot);
			client = new RealtimeMultiplayerClient(this, "99", player,botPlayers,10,2,
					new RealtimeMultiplayerEvents() {

						@Override
						public void onRoomUpdated(String roomId, final List<PlayerDetails> players) {

						}

						@Override
						public void onPlayerLeaveRoom(String roomId, final PlayerDetails player) {
							Toast.makeText(MultiplayerDemoActivity.this, player.name + " quit the game", Toast.LENGTH_SHORT).show();
							quitGame();
						}

						@Override
						public void onMessage(final PlayerDetails player, final byte[] data) {
							if (player.id.equals(opponent.id)) {
								player2TextView.setText(opponent.name + " : " + new String(data));
							}
						}

						@Override
						public void onMatchMakingDone(final String roomId, final List<PlayerDetails> players) {
							Log.d(TAG,"onMatchMakingDone()");
							isGameOn = true;
							for (PlayerDetails roomPlayer : players) {
								if (!roomPlayer.id.equals(player.id)) {
									opponent = roomPlayer;
								}
							}
							startGame();
						}

						@Override
						public void onDisconnected() {
							Log.d(TAG,"onDisconnected");
							if (!isGameOn) {
								if (pdia.isShowing()) {
									pdia.dismiss();
								}
							} else {
								Toast.makeText(MultiplayerDemoActivity.this, "Got disconnected", Toast.LENGTH_SHORT).show();
							}
							quitGame();
						}

						@Override
						public void onConnected() {
							try {
								client.matchMake("test123");
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onChatMessage(PlayerDetails player, byte[] data) {

						}

						@Override
						public void onError(MultiplayerActionType type) {
							if (type.equals(MultiplayerActionType.MATCH_MAKE)) {
								if (pdia.isShowing()) {
									pdia.dismiss();
								}
								Toast.makeText(getApplicationContext(), "No users to create a match", Toast.LENGTH_SHORT).show();
							}
						}
					});
			client.connect();
		} catch (Exception e) {
			e.printStackTrace();
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

	public void quitGame() {
		player1TextView.setText("");
		player2TextView.setText("");
		isGameOn = false;
		if (client != null && client.isConnected()) {
			client.disconnect();
		}
		client = null;
		findViewById(R.id.chatButton).setVisibility(View.GONE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			if (client != null && client.isConnected()) {
				client.leaveRoom();
				client.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		try {
			if (isGameOn && client != null) {
				client.leaveRoom();
				client.disconnect();
			}
			quitGame();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.mainButton) {
			if (client == null && !isGameOn) {
				showPromptDialog("Button Mash", "Please enter your name", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						player = new PlayerDetails();
						player.id = UUID.randomUUID().toString();
						player.name = input.getEditableText().toString();
						init();
						pdia.setMessage("Finding a player");
						pdia.show();
					}
				});
			} else if (client.isConnected() && isGameOn) {
				score++;
				try {
					player1TextView.setText(player.name + " : " + String.valueOf(score));
					client.sendMessage(String.valueOf(score).getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (view.getId() == R.id.chatButton) {
			if (client != null) {
				try {
					client.openChatRoom();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startGame() {
		pdia.dismiss();
		mainButton.setText("Click to Score");
		findViewById(R.id.chatButton).setVisibility(View.VISIBLE);

	}
}