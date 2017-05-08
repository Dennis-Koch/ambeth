package com.koch.ambeth.training.travelguides.guides;

import java.util.HashMap;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.annotation.LogCalls;

@LogCalls
public class GuideBookExtendable implements IGuideBookExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	HashMap<String, IGuideBook> books = new HashMap<String, IGuideBook>();

	@Override
	public void register(IGuideBook book, String name)
	{
		books.put(name, book);
	}

	@Override
	public void unregister(IGuideBook book, String name)
	{
		books.remove(name);
	}

	@Override
	public IGuideBook getBook(String name)
	{
		return books.get(name);
	}
}
