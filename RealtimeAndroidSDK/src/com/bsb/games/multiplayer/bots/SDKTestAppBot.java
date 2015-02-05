package com.bsb.games.multiplayer.bots;

import java.util.List;
import java.util.Random;

import android.os.Handler;
import android.widget.Toast;

import com.bsb.games.multiplayer.response.PlayerDetails;

public class SDKTestAppBot extends PlayerBot {

	private int currentScore = 0;

	private Handler handler;

	public SDKTestAppBot() {
		super();
	}

	@Override
	public void onMatchMakingDone(String roomId, List<PlayerDetails> players) {
		Toast.makeText(activity, "Match Making Done", Toast.LENGTH_SHORT).show();
		handler = new Handler(); 
        handler.postDelayed(runable, getRandomTime());
	}

	@Override
	public void onMessage(PlayerDetails player, byte[] data) {
		
	}
	
	private Runnable runable = new Runnable() { 

        @Override 
        public void run() { 
            try{
                sendMessage(String.valueOf(currentScore++).getBytes());
                handler.postDelayed(this, getRandomTime());
            }
            catch (Exception e) {
                // TODO: handle exception
            }
            finally{
                //also call the same runnable 
            }
        } 
    };
	
	private long getRandomTime() {
		Random r = new Random();
		int i1 = r.nextInt(1500 - 100) + 100;
		return i1;
	}

	@Override
	public void onPlayerLeaveRoom(String roomId, PlayerDetails player) {
		handler.removeCallbacks(runable);
	}

}
