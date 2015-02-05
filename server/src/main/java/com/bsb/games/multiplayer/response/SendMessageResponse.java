package com.bsb.games.multiplayer.response;

public class SendMessageResponse extends ActionResponse {
	public PayloadBean payload = new PayloadBean();

	public static class PayloadBean {
		public byte[] data;
		public String messageType;
		public String roomId;
		public PlayerDetails sender = new PlayerDetails();
	}
}