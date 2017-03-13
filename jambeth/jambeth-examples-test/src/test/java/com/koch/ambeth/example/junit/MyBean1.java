package com.koch.ambeth.example.junit;

import com.koch.ambeth.ioc.config.Property;

public class MyBean1
{
	private String text;

	public String getText()
	{
		return text;
	}

	@Property(name = "text.for.MyBean1", mandatory = false)
	public void setText(String text)
	{
		this.text = text;
	}
}
