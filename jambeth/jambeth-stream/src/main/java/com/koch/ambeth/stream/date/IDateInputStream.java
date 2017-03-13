package com.koch.ambeth.stream.date;

import java.util.Date;

import com.koch.ambeth.stream.IInputStream;

public interface IDateInputStream extends IInputStream
{
	boolean hasDate();

	Date readDate();
}