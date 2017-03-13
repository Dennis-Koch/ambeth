package com.koch.ambeth.stream.date;

import java.io.IOException;
import java.util.Date;

public class DateInMemoryInputStream implements IDateInputStream
{
	public static final IDateInputStream EMPTY_INPUT_STREAM = new IDateInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public Date readDate()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasDate()
		{
			return false;
		}
	};

	private int index = -1;

	private final Date[] array;

	public DateInMemoryInputStream(Date[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasDate()
	{
		return (array.length > index + 1);
	}

	@Override
	public Date readDate()
	{
		return array[++index];
	}
}
