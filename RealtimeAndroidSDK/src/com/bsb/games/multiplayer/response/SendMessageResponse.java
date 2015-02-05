package com.bsb.games.multiplayer.response;

public class SendMessageResponse {
	public PayloadBean payload;

	public static class PayloadBean {
		public byte[] data;
		public String messageType;
		public String roomId;
		public PlayerDetails sender;
	}
}