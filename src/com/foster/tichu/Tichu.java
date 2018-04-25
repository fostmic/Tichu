package com.foster.tichu;


import com.foster.tichu.TichuView.TichuThread;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class Tichu extends Activity {
	
	boolean p1grandtichu = false;
	boolean p2grandtichu = false;
	boolean p3grandtichu = false;
	boolean p4grandtichu = false;
	Object stick = new Object();

	
	TextView gametext;
	TichuThread thread;
	Handler mHandler;
	
	int dif = 0; // difficulty level


	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	protected void onStart(){
		super.onStart();
		mainmenu();
	}

	void settingsmenu()
	{
		setContentView(R.layout.settings);
		
		final Button mainbtn = (Button)findViewById(R.id.mainmenu);
		final Spinner choosedif = (Spinner) findViewById(R.id.choosedif);
		ArrayAdapter<CharSequence> theadapter = ArrayAdapter.createFromResource(this, R.array.choosedif, android.R.layout.simple_spinner_item);
		theadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		choosedif.setAdapter(theadapter);
		choosedif.setSelection(dif);
		
		mainbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				dif = choosedif.getSelectedItemPosition();
				mainmenu();

			}
		});

	}

	void mainmenu()
	{
		setContentView(R.layout.menu);

		final Button settingsBtn = (Button) findViewById(R.id.settingsbtn);
		final Button startBtn = (Button)findViewById(R.id.startbtn);

		//settings
		settingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				settingsmenu();
			}
		});
		
		startBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startgame();

			}
		});
	}
	

	
	void startgame()
	{
		
		setContentView(R.layout.ingame);
		final TichuView mTichuView = (TichuView) findViewById(R.id.tichuview);
		thread = mTichuView.getThread();
		mHandler = thread.getHandler();
		mTichuView.setstick(stick);
		mTichuView.setgtyes((Button) findViewById(R.id.gtyes));
		mTichuView.setgtno((Button) findViewById(R.id.gtno));
		mTichuView.setgametext((TextView) findViewById(R.id.gametext));
		mTichuView.settichubtn((Button) findViewById(R.id.tichubtn));
		mTichuView.setbombbtn((ImageButton) findViewById(R.id.bomb));
		mTichuView.setpassbtn((Button) findViewById(R.id.passcards));
        thread.start();
	}
}           