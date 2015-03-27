package com.bsb.games.multiplayer;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bsb.games.multiplayer.response.PlayerDetails;

public class ChatDialog extends Dialog {

	private final String TAG = getClass().getCanonicalName();
	private List<ChatMessage> chatMessages;
	private Activity activity;
	private RealtimeMultiplayerClient client;
	private RealtimeMultiplayerClient2 client2;
	private ListView chatList;
	private ChatListAdapter adapter;
	private PlayerDetails player;
	private Button sendButton;
	private EditText editText;

	public ChatDialog(Activity activity, RealtimeMultiplayerClient client, PlayerDetails player, List<ChatMessage> chatMessages) {
		super(activity);
		this.chatMessages = chatMessages;
		this.activity = activity;
		this.client = client;
		this.player = player;
	}
	
	public ChatDialog(Activity activity, RealtimeMultiplayerClient2 client, PlayerDetails player, List<ChatMessage> chatMessages) {
		super(activity);
		this.chatMessages = chatMessages;
		this.activity = activity;
		this.client2 = client;
		this.player = player;
	}

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle bundle) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getResourceIdByName(activity, "layout", "chat_dialog"));

		Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		int width = display.getWidth();
		int height = display.getHeight();
		height = (int) (height - (height * 0.2));
		Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.width = (int) (width * .85);
		window.setAttributes(wlp);
		Log.d(TAG, "height : " + height + " width : " + width);
		getWindow().setLayout(width, height);

		setCancelable(true);
		setCanceledOnTouchOutside(false);
		chatList = (ListView) findViewById(getResourceIdByName(activity, "id", "chat_list"));
		adapter = new ChatListAdapter(activity, player, chatMessages);
		chatList.setAdapter(adapter);
		sendButton = (Button) findViewById(getResourceIdByName(activity, "id", "chat_send_button"));
		editText = (EditText) findViewById(getResourceIdByName(activity, "id", "chat_edittext"));

		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editText.getEditableText().toString() != null && !editText.getEditableText().toString().equals("")) {
					try {
						if(client!=null) {
							client.sendChatMessage(editText.getEditableText().toString());
						}else {
							client2.sendChatMessage(editText.getEditableText().toString());
						}
						editText.setText("");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void setChatMessages(List<ChatMessage> chatMessages) {
		this.chatMessages = chatMessages;
		adapter = new ChatListAdapter(activity, player, this.chatMessages);
		chatList.setAdapter(adapter);
	}

	public static int getResourceIdByName(Context context, String className, String name) {
		String packageName = context.getPackageName();
		Resources res = context.getResources();
		int resource = res.getIdentifier(name, className, packageName);
		return resource;
	}

}
