package com.bsb.games.multiplayer;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bsb.games.multiplayer.response.PlayerDetails;

public class ChatListAdapter extends ArrayAdapter<ChatMessage> {

	private Activity activity;
	private List<ChatMessage> chatMessages;
	private PlayerDetails player;

	public ChatListAdapter(Activity activity, PlayerDetails player, List<ChatMessage> chatMessages) {
		super(activity, ChatDialog.getResourceIdByName(activity, "layout", "chat_listitem_me"));
		this.activity = activity;
		this.player = player;
		this.chatMessages = chatMessages;
	}

	static class ViewHolder {
		public TextView name;
		public TextView message;
	}

	@Override
	public int getItemViewType(int position) {
		ChatMessage message = chatMessages.get(position);
		if (message.player.id.equals(player.id)) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return chatMessages.size();
	}

	@Override
	public ChatMessage getItem(int position) {
		return chatMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		final ChatMessage message = chatMessages.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			if (getItemViewType(position) == 0) {
				rowView = inflater.inflate(ChatDialog.getResourceIdByName(activity, "layout", "chat_listitem_me"), null);
			} else {
				rowView = inflater.inflate(ChatDialog.getResourceIdByName(activity, "layout", "chat_listitem_they"), null);
			}
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(ChatDialog.getResourceIdByName(activity, "id", "chat_name"));
			viewHolder.message = (TextView) rowView.findViewById(ChatDialog.getResourceIdByName(activity, "id", "chat_content"));
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		if (!message.player.id.equals(player.id)) {
			holder.name.setText(message.player.name);
		}
		holder.message.setText(message.message);
		return rowView;
	}

}
