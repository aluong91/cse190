package com.example.bidit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BrowseActivity extends Activity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onClick(View view) {
		if(view instanceof Button){
			Button clicked = (Button)view;
			switch(clicked.getId()){
			case R.id.backbutton:
				Intent intent = new Intent(BrowseActivity.this, MainActivity.class);
				startActivity(intent);
				break;
			}
		}
		
	}
}
