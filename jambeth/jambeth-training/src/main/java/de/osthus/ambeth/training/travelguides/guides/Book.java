package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class Book implements IBook
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
	private String content;

	@Autowired
	IGuideBookExtendable ex;

	protected IGuideBookExtendable bookExtendable;

	@Override
	public String read()
	{

		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}
}
