package de.osthus.ambeth.stream.date;

import java.util.Date;

import de.osthus.ambeth.stream.IInputStream;

public interface IDateInputStream extends IInputStream
{
	boolean hasDate();

	Date readDate();
}