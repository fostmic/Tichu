package com.foster.tichu;

public class HandofEight
{
	public Card[] cards;
	public HandofEight()
	{
		cards = new Card[8];
		for (int i = 0; i < 8; i++)
		{
			cards[i] = new Card();
		}
	}
	
}