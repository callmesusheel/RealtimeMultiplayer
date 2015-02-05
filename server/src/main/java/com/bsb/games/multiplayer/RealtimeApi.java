package com.bsb.games.multiplayer;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.bsb.games.multiplayer.actiondata.ActionRequest;
import com.bsb.games.multiplayer.actions.CreateRoomAction;
import com.bsb.games.multiplayer.actions.ExitRoomAction;
import com.bsb.games.multiplayer.actions.JoinRoomAction;
import com.bsb.games.multiplayer.actions.MatchMakeAction;
import com.bsb.games.multiplayer.actions.SendMessageAction;
import com.bsb.games.multiplayer.properties.Player;
import com.bsb.games.multiplayer.properties.RealtimeData;
import com.bsb.games.multiplayer.properties.Room;
import com.bsb.games.multiplayer.response.ErrorResponse;
import com.google.gson.Gson;

@ServerEndpoint(value = "/realtime")
public class RealtimeApi {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	@OnOpen
	public void onOpen(Session session) {
		logger.info("Connected ... " + session.getId());
		session.setMaxIdleTimeout(60000);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			ActionRequest response = new Gson().fromJson(message, ActionRequest.class);
			switch (response.data.type) {
			case CREATE_ROOM:
				logger.info("CREATEROOM with session Id : " + session.getId());
				CreateRoomAction createRoom = new CreateRoomAction();
				createRoom.onMessage(message, session);
				break;
			case JOIN_ROOM:
				logger.info("JOINROOM");
				JoinRoomAction joinRoom = new JoinRoomAction();
				joinRoom.onMessage(message, session);
				break;
			case EXIT_ROOM:
				logger.info("EXITROOM");
				ExitRoomAction exitRoomAction = new ExitRoomAction();
				exitRoomAction.onMessage(message, session);
				break;
			case SEND_MESSAGE:
				logger.info("SENDMESSAGE");
				SendMessageAction sendMessageAction = new SendMessageAction();
				sendMessageAction.onMessage(message, session);
				break;
			case MATCH_MAKE:
				logger.info("MATCHMAKE");
				session.setMaxIdleTimeout(3 * 60000);
				MatchMakeAction matchMakeAction = new MatchMakeAction();
				matchMakeAction.onMessage(message, session);
				break;
			case PING:
				logger.info("PING");
				session.getAsyncRemote().sendText(new Gson().toJson(response));
				break;
			case DISCONNECT:
				logger.info("DISCONNECT");
				session.close();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendErrorResponse(String message, Session session) {
		ErrorResponse response = new Gson().fromJson(message, ErrorResponse.class);
		String responseString = new Gson().toJson(response);
		logger.info("sendResponseOfUserExit : " + responseString);
		session.getAsyncRemote().sendText(responseString);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
			Room room = RealtimeData.getRealtimeData().getRoom(session);
			if (room != null) {
				Player roomPlayer = room.getPlayer(session);
				RealtimeData.getRealtimeData().removeSessionFromRoom(session);
				ExitRoomAction exitRoomAction = new ExitRoomAction();
				exitRoomAction.sendResponseOfUserExit(roomPlayer, room);
			}
			RealtimeData.getRealtimeData().removeSessionFromAvailableRoom(session);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
