

# RealtimeMultiplayer #
This repo contains the server side and client SDK of realtime multiplayer and a demo app. 

##Server Deployment##
* First build the server project by running this command in server/ directory. Please ensure you have maven installed in your machine before you run this command.
	> mvn clean install -DskipTests

* The compiled jar file will be available in server/target/RealtimeMultiplayer-0.0.1-SNAPSHOT-jar-with-dependencies.jar

* Also edit these lines in the App.java file. App.java has the main function which starts the server. In the line after the server.start(), please add the lines in the snippet below.  This basically configures the state of a game in the server. Every time you want to support a new game create another instance of GameConfig add it to the map by RealtimeData.getRealtimeData().putGameConfig("< your new game id >", gameConfig);
> GameConfig gameConfig = new GameConfig(maxAllowedPlayers, minPlayersToStart, maxIdleTimeout, null); 
			RealtimeData.getRealtimeData().putGameConfig("< your game id >", gameConfig);

* At this point you're ready to deploy the server. You can do this by running this command. Make sure that you run the command in the remote machine where you want to run the server. 
> java -classpath RealtimeMultiplayer-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.bsb.games.multiplayer.App

##Client SDK##
 * Add the ***android-sdk*** project as a reference project to your game. This will allow you to integrate Realtime Multiplayer in your game.
 * Create a file called ***strings.xml*** in the directory android-sdk/res/values/. Add the following lines in that file. Also change the value of "server_link", to correspond your host address. 

			<resources>
			<string name="app_name">MultiPlayerSDK</string>
	        <string name="server_link">ws://<Your host address>:10023/multiplayer/realtime</string>
	        </resources>
 *  Now you're ready to start coding. 
There is a small snippet of code that'll get you started. Use this code to create a RealtimeMultiplayerClient object. The callbacks work as advertised. When the following code is executed, you'll get a "onConnected()" callback.  

		PlayerDetails player = new PlayerDetails();
		player.id = UUID.randomUUID().toString();
		player.name = "Susheel";
		
		RealtimeMultiplayerClient client = new RealtimeMultiplayerClient(this, ConfigManager.getString(this, "<unique gameid>"), player, new RealtimeMultiplayerEvents() {
				
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
 *  To perform automatic online ***match-making***, call the function in the snippet below. Also ensure that the unique matchmaking id that you use is same for a common set of people to be matched. Once a match is made, you'll get a callback in "onMatchMakingDone()" with the details of all the players in the room. 
> client.matchMake("< unique matchmaking Id >");
 * To ***create a room in order to host a game***, call the function in the snippet below. This will allow you to create a room so that you can pass the room id of the same to the player's friends for them to join. You'll get the room id after its creation in the callback "onRoomUpdated(String roomId, final List<PlayerDetails> players)" with the details of the players(at this point there'll just be you. However, when somebody new joins then this callback will be fired again with their details added as well). When another person wants to join this room, use the second line of the snippet. 
> client.createRoom("< unique matchmaking Id >");
> client.joinRoom("< room id >");

 * When somebody wants to quit/leave a game call the function in the snippet. Also, make sure that you call this function in the onStop() or onPause() function of your activity. This will enable the other player to know when his challenger has left playing the game.
 > client.leaveRoom();
 * To send a message to everybody in the room, please use the function in the snippet. This will broadcast the message to everybody in the room but the player who's sending it. The other players in the room will get the message in the callback which is shown in the second line of the snippet.
 > client.sendMessage(" < Your message > ".getBytes());
 > public void onMessage(final PlayerDetails player, final byte[] data)
 

    
    
    			
		
> For further help or feedback, contact [Susheel](mailto:sk@hike.in) 
