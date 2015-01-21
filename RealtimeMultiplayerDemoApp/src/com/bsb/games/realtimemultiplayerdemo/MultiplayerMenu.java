package com.bsb.games.realtimemultiplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MultiplayerMenu extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer_menu);
		
		findViewById(R.id.demoButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MultiplayerMenu.this, MultiplayerDemoActivity.class);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.testButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MultiplayerMenu.this, MultiplayerTestActivity.class);
				startActivity(intent);
			}
		});
	}

}