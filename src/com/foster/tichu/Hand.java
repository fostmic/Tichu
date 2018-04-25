package com.foster.tichu;

import java.util.ArrayList;

public class Hand
{
	public Card[] cards;
	public Card[] topass;
	public int numleft;
	ArrayList <Playable>bombs;
	ArrayList <Playable>straights;
	ArrayList <Playable>consecpairs;
	ArrayList <Playable>fullhouses;
	ArrayList <Playable>trips;
	ArrayList <Playable>pairs;
	ArrayList <Playable>singles;
	public Hand()
	{
		bombs = new ArrayList<Playable>();
		straights = new ArrayList<Playable>();
		consecpairs = new ArrayList<Playable>();
		fullhouses = new ArrayList<Playable>();
		trips = new ArrayList<Playable>();
		pairs = new ArrayList<Playable>();
		singles = new ArrayList<Playable>();
		cards = new Card[14];
		for (int i = 0; i < 14; i++)
		{
			cards[i] = new Card();
		}
		topass = new Card[3];
		for (int i = 0; i < 3; i++)
		{
			topass[i] = new Card();
		}
		numleft = 14;
	}
	public Card pull(int card)  // pull a card out of the hand
	{
		Card returncard = new Card(cards[card].rank, cards[card].suit);
		for (int i = card; i < numleft-1; i++)
			cards[i].setto(cards[i+1]);
		numleft--;
		return returncard;		
	}
	public void addcard(Card card){
		if (numleft < 14)
		{
			int i = 0;
			boolean keeplooking = true;
			while (keeplooking) {
				if (i < numleft) {
					if (cards[i].rank < card.rank)
						i++;
					else
						keeplooking = false;
				}
				else
					keeplooking = false;
			}
			for (int j = numleft; j > i; j--) {
				cards[j].rank = cards[j-1].rank;
				cards[j].suit = cards[j-1].suit;
			}
			cards[i].rank = card.rank;
			cards[i].suit = card.suit;
			numleft++;
		}
	}
}