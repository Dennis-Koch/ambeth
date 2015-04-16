package de.osthus.ambeth.training.travelguides.ioc;

import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.guides.Book;

public class BookFactory implements IFactoryBean
{

	public BookFactory(String name, Object other)
	{
	}

	public BookFactory()
	{
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	int counter = 1;
	@Autowired
	IServiceContext context;

	private Book book;

	@Override
	public Object getObject() throws Throwable
	{
		book = book == null ? new Book() : book;
		// Book book = new Book();
		book.setContent("SOME MAGIC" + counter);
		counter++;

		IBeanRuntime<Book> bookLifecycle = context.registerWithLifecycle(book);
		return bookLifecycle.finish();
	}
}
