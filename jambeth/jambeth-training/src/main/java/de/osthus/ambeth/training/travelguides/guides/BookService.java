package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.ioc.DefaultBook;

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
