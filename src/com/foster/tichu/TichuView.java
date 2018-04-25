package com.foster.tichu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;





public class TichuView extends SurfaceView implements SurfaceHolder.Callback {
	public class TichuThread extends Thread{
		private SurfaceHolder mSurfaceHolder;
		private Handler mHandler;
		private Context mContext;
		boolean p1grandtichu = false;
		boolean p2grandtichu = false;
		boolean p3grandtichu = false;
		boolean p4grandtichu = false;
		boolean p1tichu = false;
		boolean p2tichu = false;
		boolean p3tichu = false;
		boolean p4tichu = false;
		boolean cardspassed = false;
		Card tempcard = new Card();
		boolean[] selectedcard = new boolean[14];
		int lastselected = -4;  // -1, -2, -3 are for boxes to pass to other players
		boolean canbomb = false;  //set to true if a bomb can be played


		public TichuThread(SurfaceHolder holder, Context context,
				Handler handler) {
			mSurfaceHolder = holder;
			mHandler = handler;
			mContext = context;
			for (int i = 0; i < 14; i++)
				selectedcard[i] = false;
		}

		@Override
		public void run(){

			
			initbmps();
			deal();
			c = null;
			while (c == null)
			{
				try {
					c = mSurfaceHolder.lockCanvas();
					synchronized (mSurfaceHolder) {
						show8();
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
			gtyes.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					p1grandtichu = true;
					p1tichu = true;
					gtyes.setVisibility(View.INVISIBLE);
					gtno.setVisibility(View.INVISIBLE);
					changetext("", View.INVISIBLE);
					synchronized (thestick){
						thestick.notify();
					}
				}
			});

			gtno.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gtyes.setVisibility(View.INVISIBLE);
					gtno.setVisibility(View.INVISIBLE);
					changetext("", View.INVISIBLE);
					synchronized (thestick){
						thestick.notify();
					}
				}
			});

			changetext(mContext.getString(R.string.grandtichu),View.VISIBLE);
			gtyes.getHandler().post(new Runnable() {
				public void run() {
					gtyes.setVisibility(View.VISIBLE);
				}
			});
			gtno.getHandler().post(new Runnable() {
				public void run() {
					gtno.setVisibility(View.VISIBLE);
				}
			});
			synchronized (thestick)
			{
				try {
					thestick.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}


			// will computers call grand tichu?
			p2grandtichu = callgt(player2);
			p3grandtichu = callgt(player3);
			p4grandtichu = callgt(player4);

			//show messages if they do
			if (p1grandtichu)			{
				changetext(mContext.getString(R.string.p1gt), View.VISIBLE);
				try {
					sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (p2grandtichu)			{
				changetext(mContext.getString(R.string.p2gt), View.VISIBLE);
				try {
					sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (p3grandtichu)			{
				changetext(mContext.getString(R.string.p3gt), View.VISIBLE);
				try {
					sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (p4grandtichu)			{
				changetext(mContext.getString(R.string.p4gt), View.VISIBLE);
				try {
					sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			c = null;
			while (c == null)
			{
				try {
					c = mSurfaceHolder.lockCanvas();
					synchronized (mSurfaceHolder) {
						showall();
						drawboxes();
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}

			if (!p1tichu) {

				calltichu.getHandler().post(new Runnable() {
					public void run() {
						calltichu.setVisibility(View.VISIBLE);
					}
				});
			}

			calltichu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					p1tichu = true;
					changetext(mContext.getString(R.string.p1tichu), View.VISIBLE);
					calltichu.setVisibility(View.INVISIBLE);
					gametext.getHandler().postDelayed(new Runnable() {
						public void run() {
							gametext.setVisibility(View.INVISIBLE);
						}
					}, DELAY);
				}
			});

			changetext(mContext.getString(R.string.selectcards), View.VISIBLE);
			setOnTouchListener(touchHandler);
			
			//parse hands into playables
			parsehand(player1); 
			parsehand(player2);
			parsehand(player3);
			parsehand(player4);
		 	
			while (!cardspassed){
				synchronized (thestick)
				{
					try {
						thestick.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				c = null;
				while (c == null)
				{
					try {
						c = mSurfaceHolder.lockCanvas();
						synchronized (mSurfaceHolder) {
							showall();
							drawboxes();
						}
					} finally {
						// do this in a finally so that if an exception is thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}
				}
			}
		}

		private OnTouchListener touchHandler = new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				float x, y;
				x = me.getX();
				y = me.getY();
				if (x > width * 3/12 && x < width * 3/12 + player1.numleft * width/30 + 36 && y > height * 8/10)
				{
					if (lastselected > -4 && lastselected < 0 && !cardspassed){ // move card from box back to hand	
						if (player1.topass[lastselected+3].rank > -1){
							player1.addcard(player1.topass[lastselected+3]);
							player1.topass[lastselected+3].rank = -1;
							player1.topass[lastselected+3].suit = -1;
						}
					}

					lastselected = (int) ((x - ((3f/12f) * width)) / (width/30f));
					if (lastselected > 13)
						lastselected = 13;
					selectedcard[lastselected] = !selectedcard[lastselected];
				}
				else if (x > width*6/20 && x < width*6/20+72 && y > height*1/2 && y < height*1/2+96) {
					if (lastselected > -1) {
						if (player1.topass[0].rank > -1) {  // if there is already a card in the box switch cards
							tempcard.setto(player1.topass[0]);
							player1.topass[0].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
							player1.addcard(tempcard);
						}
						else{   // no card in the box
							player1.topass[0].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
						}
						lastselected = -4;
					}
					else if (lastselected > -3) // if switching cards to pass between players
					{
						if (player1.topass[lastselected+3].rank > -1) {
							tempcard.setto(player1.topass[0]);
							player1.topass[0].setto(player1.topass[lastselected+3]);
							player1.topass[lastselected+3].setto(tempcard);
						}
						else{
							player1.topass[lastselected+3].setto(player1.topass[0]);
							player1.topass[0].setto(new Card());
						}
						lastselected = -4;
					}
					else
						lastselected = -3;
					for (int i = 0; i < 14; i++)
						selectedcard[i] = false;
				}
				else if (x > width*9/20 && x < width*9/20+72 && y > height*7/20 && y < height*7/20+96) {
					if (lastselected > -1) {
						if (player1.topass[1].rank > -1) {  // if there is already a card in the box switch cards
							tempcard.setto(player1.topass[1]);
							player1.topass[1].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
							player1.addcard(tempcard);
						}
						else{   // no card in the box
							player1.topass[1].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
						}
						lastselected = -4;
					}
					else if (lastselected == -3 || lastselected == -1) // if switching cards to pass between players
					{
						if (player1.topass[lastselected+3].rank > -1) {
							tempcard.setto(player1.topass[1]);
							player1.topass[1].setto(player1.topass[lastselected+3]);
							player1.topass[lastselected+3].setto(tempcard);
						}
						else{
							player1.topass[lastselected+3].setto(player1.topass[1]);
							player1.topass[1].setto(new Card());
						}
						lastselected = -4;
					}
					else
						lastselected = -2;
					for (int i = 0; i < 14; i++)
						selectedcard[i] = false;
				}
				else if (x > width*12/20 && x < width*12/20+72 && y > height*1/2 && y < height*1/2+96) {
					if (lastselected > -1) {
						if (player1.topass[2].rank > -1) {  // if there is already a card in the box switch cards
							tempcard.setto(player1.topass[2]);
							player1.topass[2].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
							player1.addcard(tempcard);
						}
						else{   // no card in the box
							player1.topass[2].setto(player1.cards[lastselected]);
							player1.pull(lastselected);
						}
						lastselected = -4;
					}
					else if (lastselected == -3 || lastselected == -2) // if switching cards to pass between players
					{
						if (player1.topass[lastselected+3].rank > -1) {
							tempcard.setto(player1.topass[2]);
							player1.topass[2].setto(player1.topass[lastselected+3]);
							player1.topass[lastselected+3].setto(tempcard);
						}
						else{
							player1.topass[lastselected+3].setto(player1.topass[2]);
							player1.topass[2].setto(new Card());
						}
						lastselected = -4;
					}
					else
						lastselected = -1;
					for (int i = 0; i < 14; i++)
						selectedcard[i] = false;
				}
				if (player1.numleft == 11){
					passbtn.setVisibility(View.VISIBLE);
					passbtn.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							passbtn.setVisibility(INVISIBLE);
							
					//		whattopass(player2);
					//		whattopass(player3);
					//		whattopass(player4);
							cardspassed = true;
							//TODO continue
						}
					});
					
				}
				synchronized (thestick){
					thestick.notify();
				}
				return false;
			}

		};

		public void initbmps()
		{ 

			cardback = ((BitmapDrawable) mResources.getDrawable(R.drawable.back) ).getBitmap(); 	
			cardbmp[0][0] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c2) ).getBitmap();
			cardbmp[0][1] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c3) ).getBitmap();
			cardbmp[0][2] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c4) ).getBitmap();
			cardbmp[0][3] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c5) ).getBitmap();
			cardbmp[0][4] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c6) ).getBitmap();
			cardbmp[0][5] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c7) ).getBitmap();
			cardbmp[0][6] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c8) ).getBitmap();
			cardbmp[0][7] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c9) ).getBitmap();
			cardbmp[0][8] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c10) ).getBitmap();
			cardbmp[0][9] = ((BitmapDrawable) mResources.getDrawable(R.drawable.cj) ).getBitmap();
			cardbmp[0][10] = ((BitmapDrawable) mResources.getDrawable(R.drawable.cq) ).getBitmap();
			cardbmp[0][11] = ((BitmapDrawable) mResources.getDrawable(R.drawable.ck) ).getBitmap();
			cardbmp[0][12] = ((BitmapDrawable) mResources.getDrawable(R.drawable.c1) ).getBitmap();
			cardbmp[0][13] = ((BitmapDrawable) mResources.getDrawable(R.drawable.dog) ).getBitmap();
			cardbmp[1][0] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d2) ).getBitmap();
			cardbmp[1][1] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d3) ).getBitmap();
			cardbmp[1][2] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d4) ).getBitmap();
			cardbmp[1][3] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d5) ).getBitmap();
			cardbmp[1][4] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d6) ).getBitmap();
			cardbmp[1][5] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d7) ).getBitmap();
			cardbmp[1][6] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d8) ).getBitmap();
			cardbmp[1][7] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d9) ).getBitmap();
			cardbmp[1][8] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d10) ).getBitmap();
			cardbmp[1][9] = ((BitmapDrawable) mResources.getDrawable(R.drawable.dj) ).getBitmap();
			cardbmp[1][10] = ((BitmapDrawable) mResources.getDrawable(R.drawable.dq) ).getBitmap();
			cardbmp[1][11] = ((BitmapDrawable) mResources.getDrawable(R.drawable.dk) ).getBitmap();
			cardbmp[1][12] = ((BitmapDrawable) mResources.getDrawable(R.drawable.d1) ).getBitmap();
			cardbmp[1][13] = ((BitmapDrawable) mResources.getDrawable(R.drawable.sparrow) ).getBitmap();
			cardbmp[2][0] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h2) ).getBitmap();
			cardbmp[2][1] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h3) ).getBitmap();
			cardbmp[2][2] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h4) ).getBitmap();
			cardbmp[2][3] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h5) ).getBitmap();
			cardbmp[2][4] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h6) ).getBitmap();
			cardbmp[2][5] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h7) ).getBitmap();
			cardbmp[2][6] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h8) ).getBitmap();
			cardbmp[2][7] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h9) ).getBitmap();
			cardbmp[2][8] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h10) ).getBitmap();
			cardbmp[2][9] = ((BitmapDrawable) mResources.getDrawable(R.drawable.hj) ).getBitmap();
			cardbmp[2][10] = ((BitmapDrawable) mResources.getDrawable(R.drawable.hq) ).getBitmap();
			cardbmp[2][11] = ((BitmapDrawable) mResources.getDrawable(R.drawable.hk) ).getBitmap();
			cardbmp[2][12] = ((BitmapDrawable) mResources.getDrawable(R.drawable.h1) ).getBitmap();
			cardbmp[2][13] = ((BitmapDrawable) mResources.getDrawable(R.drawable.phoenix) ).getBitmap();
			cardbmp[3][0] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s2) ).getBitmap();
			cardbmp[3][1] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s3) ).getBitmap();
			cardbmp[3][2] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s4) ).getBitmap();
			cardbmp[3][3] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s5) ).getBitmap();
			cardbmp[3][4] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s6) ).getBitmap();
			cardbmp[3][5] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s7) ).getBitmap();
			cardbmp[3][6] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s8) ).getBitmap();
			cardbmp[3][7] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s9) ).getBitmap();
			cardbmp[3][8] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s10) ).getBitmap();
			cardbmp[3][9] = ((BitmapDrawable) mResources.getDrawable(R.drawable.sj) ).getBitmap();
			cardbmp[3][10] = ((BitmapDrawable) mResources.getDrawable(R.drawable.sq) ).getBitmap();
			cardbmp[3][11] = ((BitmapDrawable) mResources.getDrawable(R.drawable.sk) ).getBitmap();
			cardbmp[3][12] = ((BitmapDrawable) mResources.getDrawable(R.drawable.s1) ).getBitmap();
			cardbmp[3][13] = ((BitmapDrawable) mResources.getDrawable(R.drawable.dragon) ).getBitmap();
		}



		//deal out cards:  start with 2 of clubs, give to random player, repeat
		public void deal()
		{
			Random r = new Random();
			int p1rem = 14;
			int p2rem = 14;
			int p3rem = 14;
			int p4rem = 14;
			int eight1 = 8;
			int eight2 = 8;
			int eight3 = 8;
			int eight4 = 8;
			int rand;
			int rand2;
			for (int i = 0; i < 56; i++)
			{
				rand = r.nextInt(56-i);
				if (rand < p1rem)
				{
					rand2 = r.nextInt(p1rem);
					player1.cards[14-p1rem].rank = (short) (i / 4);
					player1.cards[14-p1rem].suit = (short) (i % 4);
					if (rand2 < eight1)
					{
						eight1--;
						eightcards1.cards[7-eight1].rank = (short) (i / 4);
						eightcards1.cards[7-eight1].suit = (short) (i % 4);
					}
					p1rem--;
				}
				else if (rand < p1rem + p2rem)
				{
					rand2 = r.nextInt(p2rem);
					player2.cards[14-p2rem].rank = (short) (i / 4);
					player2.cards[14-p2rem].suit = (short) (i % 4);
					if (rand2 < eight2)
					{
						eight2--;
						eightcards2.cards[7-eight2].rank = (short) (i / 4);
						eightcards2.cards[7-eight2].suit = (short) (i % 4);
					}
					p2rem--;
				}
				else if (rand < p1rem + p2rem + p3rem)
				{
					rand2 = r.nextInt(p3rem);
					player3.cards[14-p3rem].rank = (short) (i / 4);
					player3.cards[14-p3rem].suit = (short) (i % 4);
					if (p3rem > 6)
					{
						eight3--;
						eightcards3.cards[7-eight3].rank = (short) (i / 4);
						eightcards3.cards[7-eight3].suit = (short) (i % 4);
					}
					p3rem--;
				}
				else
				{
					rand2 = r.nextInt(p4rem);
					player4.cards[14-p4rem].rank = (short) (i / 4);
					player4.cards[14-p4rem].suit = (short) (i % 4);
					if (p4rem > 6)
					{
						eight4--;
						eightcards4.cards[7-eight4].rank = (short) (i / 4);
						eightcards4.cards[7-eight4].suit = (short) (i % 4);
					}
					p4rem--;
				}
			}
			player1.cards[0] = new Card((short)0,(short)0);
			player1.cards[1] = new Card((short)1,(short)0);
			player1.cards[2] = new Card((short)2,(short)0);
			player1.cards[3] = new Card((short)3,(short)1);
			player1.cards[4] = new Card((short)4,(short)0);
			player1.cards[5] = new Card((short)8,(short)1);
			player1.cards[6] = new Card((short)8,(short)2);
			player1.cards[7] = new Card((short)9,(short)3);
			player1.cards[8] = new Card((short)9,(short)2);
			player1.cards[9] = new Card((short)10,(short)3);
			player1.cards[10] = new Card((short)13,(short)0);
			player1.cards[11] = new Card((short)13,(short)1);
			player1.cards[12] = new Card((short)13,(short)2);
			player1.cards[13] = new Card((short)13,(short)3);
			//TODO - remove - testing only
		}

		public void show8() {
			c.drawColor(Color.BLACK);
			thematrix.setTranslate(width*2/10, height*2/10);  // draw player1 and 2
			thematrix.preRotate(90.0f);
			for (int i = 0; i < 8; i++)			{
				c.drawBitmap(cardback, thematrix, null);
				c.drawBitmap(cardbmp[eightcards1.cards[i].suit][eightcards1.cards[i].rank], width*4/12+i*width/30, height*8/10, null);
				thematrix.preTranslate(height/20,0);
			}

			thematrix.setTranslate(width*8/10, height*6/20); //draw player4
			thematrix.preRotate(270.0f);
			for (int i = 0; i < 8; i++)			{
				c.drawBitmap(cardback, thematrix, null);
				thematrix.preTranslate(-height/20,0);
			}
			thematrix.setTranslate(width*8/20, height*3/20);  //draw player3
			thematrix.preRotate(180.0f);
			for (int i = 0; i < 8; i++) 		{	
				c.drawBitmap(cardback, thematrix, null);
				thematrix.preTranslate(-width/30, 0);
			}
		}

		public void showall() {

			c.drawColor(Color.BLACK);  

			//draw player 1's cards 
			for (int i = 0; i < player1.numleft; i++){
				if (selectedcard[i] == false)
					c.drawBitmap(cardbmp[player1.cards[i].suit][player1.cards[i].rank], width*3/12+i*width/30, height*8/10, null);
			}
			for (int i = 0; i < player1.numleft; i++){
				if (selectedcard[i] == true) // if card is selected
					c.drawBitmap(cardbmp[player1.cards[i].suit][player1.cards[i].rank], width*3/12+i*width/30, height*15/20, null);
			}

			thematrix.setTranslate(width*2/10, height*2/10);  // draw player2
			thematrix.preRotate(90.0f);
			for (int i = 0; i < player2.numleft; i++)
			{
				c.drawBitmap(cardback, thematrix, null);
				thematrix.preTranslate(height/30,0);
			}

			thematrix.setTranslate(width*8/10, height*6/20); //draw player4
			thematrix.preRotate(270.0f);
			for (int i = 0; i < player4.numleft; i++) 
			{
				c.drawBitmap(cardback, thematrix, null);
				thematrix.preTranslate(-height/30,0);
			}


			thematrix.setTranslate(width*6/20, height*3/20);  //draw player3
			thematrix.preRotate(180.0f);
			for (int i = 0; i < player3.numleft; i++) 
			{
				c.drawBitmap(cardback, thematrix, null);
				thematrix.preTranslate(-width/30, 0);
			}	
		}

		public void drawboxes(){
			Paint p = new Paint();
			int box1w = width * 6/20;
			int box1h = height * 10/20;
			int box2w = width * 9/20;
			int box2h = height * 7/20;
			int box3w = width * 12/20;
			int box3h = height * 10/20;
			p.setColor(Color.GREEN);
			p.setStyle(Paint.Style.STROKE);
			c.drawRect(box1w, box1h, box1w + 72, box1h + 96, p);
			c.drawRect(box2w, box2h, box2w + 72, box2h + 96, p);
			c.drawRect(box3w, box3h, box3w + 72, box3h + 96, p);
			if (player1.topass[0].rank > -1)
				c.drawBitmap(cardbmp[player1.topass[0].suit][player1.topass[0].rank], width*6/20, height*1/2, null);
			if (player1.topass[1].rank > -1)
				c.drawBitmap(cardbmp[player1.topass[1].suit][player1.topass[1].rank], width*9/20, height*7/20, null);
			if (player1.topass[2].rank > -1)
				c.drawBitmap(cardbmp[player1.topass[2].suit][player1.topass[2].rank], width*12/20, height*1/2, null);

		}

		boolean callgt(Hand mhand) //returns true if hand should call grand tichu
		{
			/*	short goodcards = 0;
			for (int i = 0; i < 8; i++)
			{
				if (mhand.cards[i].rank > 11)  //count aces, special cards
					goodcards++;
			}
			if (goodcards > 2)
				return true;
			else
				return false;
			 */
			return true;	
		}
		
		void whattopass(Hand mhand)  //TODO do this
		{
		
		}
		void parsehand(Hand mHand) 
		{
 			ArrayList<Short> newlist = new ArrayList<Short>();
			short wildsused = 0;
			short temprank;
			short tempsuit;
			boolean theend = false;
			short ii;
			short quadbombcards = 1;
			short phoenixloc = -1;
			short sparrowloc = -1;
			short[][] values; // 2d array of shorts referencing card numbers in hand that maps card values to cards in hand
			short[] index;
			//see if you have the phoenix
			if (mHand.cards[13].rank == 13)			{
				if (mHand.cards[13].suit == 2)				
					phoenixloc = 13;
				else if (mHand.cards[13].suit == 3)
					if (mHand.cards[12].rank == 13 && mHand.cards[12].suit == 2) 
						phoenixloc = 12;
			}
			
			// add singles to list of playables
			for (short i = 0; i < 14; i++)			{
				newlist.clear();
				newlist.add(i);
				mHand.singles.add(new Playable(newlist));
				mHand.cards[i].partof.add(new ShortPair(SINGLE,i));
			}
			
			// add quad bombs to list of playables
			temprank = mHand.cards[0].rank;
			for (int i = 1; i < 14; i++)			{
				if (mHand.cards[i].rank == temprank && mHand.cards[i].rank < 13)				{
					quadbombcards++;
					if (quadbombcards == 4) //quad bomb found
					{
						newlist.clear();
						for (int j = i-3; j <= i; j++)
						{
							newlist.add((short) j);
							mHand.cards[j].partof.add(new ShortPair(BOMB, (short)(mHand.bombs.size())));
						}
						mHand.bombs.add(new Playable(newlist));
					}
				}	
				else				{
					quadbombcards = 1;
					temprank = mHand.cards[i].rank;
				}
			}
			
			//add pairs to list of playables
			temprank = mHand.cards[0].rank;
			for (int i = 1; i < 14; i ++)
			{
				if (mHand.cards[i].rank == temprank && temprank < 13)
				{
					newlist.clear();
					newlist.add((short) (i-1));
					newlist.add((short)i);
					mHand.cards[i-1].partof.add(new ShortPair(PAIR, (short)(mHand.pairs.size())));
					mHand.cards[i].partof.add(new ShortPair(PAIR, (short)(mHand.pairs.size())));
					mHand.pairs.add(new Playable(newlist));
					if (phoenixloc != -1) //if you have the phoenix, you can make trips so add those
					{
						newlist.add(phoenixloc);
						mHand.cards[i-1].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
						mHand.cards[i].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
						mHand.cards[phoenixloc].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
						mHand.trips.add(new Playable(newlist));
					}
				}
				temprank = mHand.cards[i].rank;
			}
			if (phoenixloc != -1)          //if you have a wild everything is a pair
			{
				for (int i = 0; i < phoenixloc; i++)
				{
					if (mHand.cards[i].rank < 13)
					{
						newlist.clear();
						newlist.add((short)i);
						newlist.add((short)phoenixloc);
						mHand.cards[i].partof.add(new ShortPair(PAIR, (short)(mHand.pairs.size())));
						mHand.cards[phoenixloc].partof.add(new ShortPair(PAIR, (short)(mHand.pairs.size())));
						mHand.pairs.add(new Playable(newlist));
					}
				}				
			}
			//add trips. wild trips are added in pairs section
			temprank = mHand.cards[0].rank;
			for (int i = 1; i < 13; i ++)
			{
				if (mHand.cards[i].rank == temprank && mHand.cards[i+1].rank == temprank && temprank < 13)
				{
					newlist.clear();
					newlist.add((short) (i-1));
					newlist.add((short)i);
					newlist.add((short)(i+1));
					mHand.cards[i-1].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
					mHand.cards[i].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
					mHand.cards[i+1].partof.add(new ShortPair(TRIPS, (short)(mHand.trips.size())));
					mHand.trips.add(new Playable(newlist));
				}
				temprank = mHand.cards[i].rank;
			}

			//add full houses
			for (Playable pair : mHand.pairs)
			{
				for (Playable trip : mHand.trips)
				{
					wildsused = 0;
					if (mHand.cards[trip.cards.get(2)].rank == 13)
							wildsused++;

					if (mHand.cards[pair.cards.get(1)].rank == 13)
						wildsused++;

					if (wildsused < 2 && (mHand.cards[pair.cards.get(0)].rank != mHand.cards[trip.cards.get(0)].rank))
					{

						newlist.clear();
						for (short card : trip.cards)
						{
							newlist.add((short) card);
							mHand.cards[card].partof.add(new ShortPair(FULLHOUSE, (short)(mHand.fullhouses.size())));
						}
						for (short card : pair.cards)
						{
							newlist.add((short) card);
							mHand.cards[card].partof.add(new ShortPair(FULLHOUSE, (short)(mHand.fullhouses.size())));
						}
						mHand.fullhouses.add(new Playable(newlist));
					}
				}
			}

			// add consecutive pairs
			for (int i = 0; i < mHand.pairs.size(); i++)		
			{
				for (int j = i+1; j < mHand.pairs.size(); j++)
				{
					if (mHand.cards[mHand.pairs.get(i).cards.get(1)].rank + mHand.cards[mHand.pairs.get(j).cards.get(1)].rank < 26)
						//can't use the phoenix twice
					{
						if (Math.abs(mHand.cards[mHand.pairs.get(i).cards.get(0)].rank - mHand.cards[mHand.pairs.get(j).cards.get(0)].rank) == 1)
							//pairs are consecutive
						{
							newlist.clear();
							newlist.add((short) mHand.pairs.get(i).cards.get(0));
							newlist.add((short) mHand.pairs.get(i).cards.get(1));
							newlist.add((short) mHand.pairs.get(j).cards.get(0));
							newlist.add((short) mHand.pairs.get(j).cards.get(1));
							mHand.cards[newlist.get(0)].partof.add(new ShortPair(CONSECPAIRS, (short)(mHand.consecpairs.size())));
							mHand.cards[newlist.get(1)].partof.add(new ShortPair(CONSECPAIRS, (short)(mHand.consecpairs.size())));
							mHand.cards[newlist.get(2)].partof.add(new ShortPair(CONSECPAIRS, (short)(mHand.consecpairs.size())));
							mHand.cards[newlist.get(3)].partof.add(new ShortPair(CONSECPAIRS, (short)(mHand.consecpairs.size())));
							mHand.consecpairs.add(new Playable(newlist));
						}
					}
				}
			}
			
			//add straights
			values = new short[14][4];
			for (int i = 0; i < 14; i++)
				for (int j = 0; j < 4; j++)
					values[i][j] = -1;
		
			index = new short[14];
			for (int i = 0; i < 14; i++)
				index[i] = 0;
			
			if (phoenixloc != -1)
				wildsused = 0;
			else
				wildsused = 1;
			
			for (short i = 0; i < 14; i++)     //load the values array, arrange 2's, 3's, etc in an array
			{
				ii = 0;
				temprank = mHand.cards[i].rank;
				if (temprank < 13){
					while (values[temprank][ii] != -1)
						ii++;
					values[temprank][ii] = i;			
				}
			}
			
			for (short i = 13; mHand.cards[i].rank == 13; i--)			{
				if (mHand.cards[i].suit == 1){ 		//if hand has sparrow				
					sparrowloc = i;
					temprank = -1;
					ii = 0;
					while (values[ii][index[ii]] != -1 || wildsused == 0)		{
						if (values[ii][index[ii]] == -1)
							wildsused++;
						if (ii - temprank > 3){                     //has str8							
							boolean keepgoing = true;
							while (keepgoing){
								keepgoing = false;
								newlist.clear();
								newlist.add(sparrowloc);
								mHand.cards[sparrowloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
								for (int j = 0; j <= ii; j++){
									if (values[j][index[j]] != -1){
										newlist.add(values[j][index[j]]);
										mHand.cards[values[j][index[j]]].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
									}
									else{
										newlist.add(phoenixloc);
										mHand.cards[phoenixloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
									}
								}
								mHand.straights.add(new Playable(newlist));
								if (wildsused == 0)
								{
									//add the phoenix substitution combos!
									for (int k = 0; k <= ii; k++){
										newlist.clear();
										newlist.add(sparrowloc);
										mHand.cards[sparrowloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
										for (int j = 0; j <= ii; j++)	{
											if (j == k){
												newlist.add(phoenixloc);
												mHand.cards[phoenixloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));	
											}
											else{
												newlist.add(values[j][index[j]]);
												mHand.cards[values[j][index[j]]].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
											}
										}
										mHand.straights.add(new Playable(newlist));
									}
								}
								for (int j = 0; j <= ii; j++){
									if (index[j] < 3){
										if (values[j][index[j]+1] != -1){
											index[j]++;
											for (int k = 0; k < j; k++)
												index[k] = 0;				//count all the combos of str8s		
											keepgoing = true;
											break;
										}
									}
								}						
							}
							for (int j = 0; j < 14; j++)
								index[j] = 0;
						}
						ii++;
					}
				}
			}
			for (temprank = 0; temprank < 9; temprank++)
			{
				if (phoenixloc != -1)
					wildsused = 0;
				else
					wildsused = 1;
				ii = temprank;
				while ((values[ii][index[ii]] != -1 || wildsused == 0) && !theend)					{
					if (values[ii][index[ii]] == -1)
						wildsused++;
					if (ii - temprank > 3){                     //has str8							
						boolean keepgoing = true;
						while (keepgoing){
							boolean hasbomb = true;
							keepgoing = false;
							newlist.clear();
							if (values[temprank][index[temprank]] != -1)
								tempsuit = mHand.cards[values[temprank][index[temprank]]].suit;
							else
								tempsuit = -1;
							for (int j = temprank + 1; j <= ii; j++){
								if (values[j][index[j]] == -1)
									hasbomb = false;
								else if (mHand.cards[values[j][index[j]]].suit != tempsuit)
									hasbomb = false;
							}
							if (hasbomb){
								for (int j = temprank; j <= ii; j++){
									newlist.add(values[j][index[j]]);
									mHand.cards[values[j][index[j]]].partof.add(new ShortPair(BOMB, (short)(mHand.bombs.size())));
								}
								mHand.bombs.add(new Playable(newlist));
							}
							else{
								for (int j = temprank; j <= ii; j++){
									if (values[j][index[j]] != -1){
										newlist.add(values[j][index[j]]);
										mHand.cards[values[j][index[j]]].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
									}
									else{
										newlist.add(phoenixloc);
										mHand.cards[phoenixloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
									}
								}
								mHand.straights.add(new Playable(newlist));
								if (wildsused == 0)
								{
									//add the phoenix substitution combos!
									for (int k = temprank; k <= ii; k++){
										newlist.clear();
										for (int j = temprank; j <= ii; j++)	{
											if (j == k){
												newlist.add(phoenixloc);
												mHand.cards[phoenixloc].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));	
											}
											else{
												newlist.add(values[j][index[j]]);
												mHand.cards[values[j][index[j]]].partof.add(new ShortPair(STRAIGHT, (short)(mHand.straights.size())));
											}
										}
										mHand.straights.add(new Playable(newlist));
									}
								}

							}
							for (int j = temprank; j <= ii; j++){
								if (index[j] < 3){
									if (values[j][index[j]+1] != -1){
										index[j]++;
										for (int k = 0; k < j; k++)
											index[k] = 0;				//count all the combos of str8s TODO: add phoenix substitution combos if you have the card but want to use phoenix anyway
										keepgoing = true;
										break;
									}
								}
							}						
						}
						for (int j = 0; j < 14; j++)
							index[j] = 0;
					}
					if (ii < 12)
						ii++;
					else 
						theend = true;
				}
			}
		}
		


		public void changetext(final String s, final int vis)
		{
			textHandler.post(new Runnable() {
				public void run() {
					gametext.setVisibility(vis);
					gametext.setText(s);
				}
			});
		}
		
		
		Handler getHandler()
		{
			return mHandler;
		}

	}
	private static final int DELAY = 700; //how long messages are shown on the screen in ms
	public static final short BOMB = 0;
	public static final short STRAIGHT = 1;
	public static final short CONSECPAIRS = 2;
	public static final short FULLHOUSE = 3;
	public static final short TRIPS = 4;
	public static final short PAIR = 5;
	public static final short SINGLE = 6;
	protected Matrix thematrix = new Matrix();
	protected Bitmap[][] cardbmp = new Bitmap[4][14];
	protected Bitmap cardback;
	protected int flag = 0;
	protected int height = 0;
	protected int width = 0;
	protected Hand player1 = new Hand();
	protected Hand player2 = new Hand();
	protected Hand player3 = new Hand();
	protected Hand player4 = new Hand();
	protected HandofEight eightcards1 = new HandofEight();
	protected HandofEight eightcards2 = new HandofEight();
	protected HandofEight eightcards3 = new HandofEight();
	protected HandofEight eightcards4 = new HandofEight();
	private TichuThread thread;
	protected TextView gametext;
	protected Resources mResources;
	protected Button gtyes;
	protected Button gtno;
	protected Button calltichu;
	protected Button passbtn;
	protected ImageButton bombit;
	Canvas c = null;
	protected Handler textHandler;
	protected Object thestick;



	public TichuView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		//register interest in hearing about changes to the surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		mResources = this.getContext().getResources();
		thread = new TichuThread(holder, context, new Handler());
	}



	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		height = h;
		width = w;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public TichuThread getThread() {
		return thread;
	}

	public void setgtyes(Button btn) {
		gtyes = btn;
	}

	public void setgtno(Button btn) {
		gtno = btn;
	}

	public void settichubtn(Button btn) {
		calltichu = btn;
	}

	public void setbombbtn(ImageButton btn) {
		bombit = btn;
	}

	public void setgametext(TextView tv) {
		gametext = tv;
		textHandler = tv.getHandler();	
	}
	public void setstick(Object stick){
		thestick = stick;
	}

	public void setpassbtn(Button btn) {
		passbtn = btn;
	}
}
