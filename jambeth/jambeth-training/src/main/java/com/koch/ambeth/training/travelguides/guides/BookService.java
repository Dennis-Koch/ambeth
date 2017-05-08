package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.ioc.DefaultBook;

/**
 * The book service delivers books by cities.
 */
public class BookService implements IBookService
{
	@LogInstance
	private ILogger log;

	/**
	 * the extendable storing the books
	 */
	@Autowired
	IGuideBookExtendable guideBookExtendable;

	DefaultBook defaultBook;

	@Override
	public IGuideBook getBook(String cityName)
	{

		// check if we have that book

		IGuideBook book = guideBookExtendable.getBook(cityName);

		if (book == null)
		{
			// use the default book;
			book = defaultBook;
		}
		return book;
	}
}
