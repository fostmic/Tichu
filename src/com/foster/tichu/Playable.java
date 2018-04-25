package com.foster.tichu;

import java.util.ArrayList;

public class Playable
{
	ArrayList <Short>cards = new ArrayList<Short>(1); // list of shorts that reference cards in hand
	//ArrayList <Playable>overlaps = null;
	
	@SuppressWarnings("unchecked")
	public Playable(ArrayList <Short>ncards)
	{
		cards = (ArrayList<Short>) ncards.clone();
	}
}