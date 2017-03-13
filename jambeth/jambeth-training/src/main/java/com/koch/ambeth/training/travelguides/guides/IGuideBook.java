package com.koch.ambeth.training.travelguides.guides;

public interface IGuideBook
{
	/**
	 * default direction, means the place is not defined
	 */
	public static final int NO_DIRECTION = -1;

	public int guideUs(String place);

	public void removePlace(String place);

	public void addPlace(String place, Integer dir);

}
