package com.koch.ambeth.training.travelguides.ioc;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.guides.Book;

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
