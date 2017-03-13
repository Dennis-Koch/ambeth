package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
