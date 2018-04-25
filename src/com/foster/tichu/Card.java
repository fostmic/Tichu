package com.foster.tichu;

import java.util.ArrayList;

public class Card
{
	public short rank;
	public short suit;
	public ArrayList<ShortPair> partof;
	
	public Card(short newrank, short newsuit)
	{
		rank = newrank;
		suit = newsuit;
		partof = new ArrayList<ShortPair>();  //keeps track of all the playables the card is part of
	}
	
	public Card()
	{
		rank = -1;
		suit = -1;
		partof = new ArrayList<ShortPair>();  //keeps track of all the playables the card is part of
	}
	
	public boolean isGreaterThan(Card othercard)
	{
		if ((suit + rank * 4) > (othercard.suit + othercard.rank * 4))
			return true;
		else
			return false;
	}
	public void setto(Card card)
	{
		rank = card.rank;
		suit = card.suit;
	}
}