package com.bsb.games.realtimedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MultiplayerMainMenu extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		
		findViewById(R.id.demoButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MultiplayerMainMenu.this, MultiplayerDemoActivity.class);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.testButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MultiplayerMainMenu.this, MultiplayerTestActivity.class);
				startActivity(intent);
			}
		});
	}

}
